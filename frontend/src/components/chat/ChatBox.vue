<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <div class="avatar">🤖</div>
        <div>
          <h3 class="title" style="margin:0;">AI Агент</h3>
          <div class="subtitle" style="font-size:12px;">Помощник по жалобам</div>
        </div>
      </div>
    </div>
    
    <div class="messages" ref="list">
      <div v-for="(m,i) in messages" :key="i" class="message-wrapper" :class="m.role">
        <div class="message-bubble">
          <div v-if="m.image" class="message-image">
            <img :src="m.image" alt="Uploaded photo" />
          </div>
          <div v-if="m.role === 'assistant' && m.data" class="ai-content">
            <AIResponseCard :data="m.data" />
          </div>
          <div v-else class="message-text">{{ m.text }}</div>
        </div>
      </div>
    </div>
    
    <div v-if="showMap" class="map-container">
      <ChatMapPicker v-model:lat="lat" v-model:lng="lng" />
    </div>

    <!-- Photo Preview -->
    <div v-if="photoPreview" class="photo-preview-container">
      <div class="photo-preview-card">
        <button class="photo-close" @click="cancelPhoto">✕</button>
        <img :src="photoPreview" alt="Preview" class="photo-preview-img" />
        <input 
          v-model="photoText" 
          type="text" 
          placeholder="Добавьте описание к фото (необязательно)"
          class="photo-text-input"
        />
        <button @click="sendPhoto" class="photo-send-btn">
          Отправить фото
        </button>
      </div>
    </div>
    
    <div class="input-area">
      <div class="input-wrapper">
        <input 
          v-model="text" 
          type="text"
          placeholder="Опишите проблему... номер маршрута, место, время" 
          @keyup.enter="send"
          class="chat-input"
        />
        <div class="button-group">
          <button @click="send" class="send-btn" :disabled="!text.trim()">
            <span>➤</span>
          </button>
        </div>
      </div>
      <div class="action-buttons">
        <button @click="toggleRecord" class="action-btn voice-btn" :class="{recording: isRecording}">
          <span class="voice-icon">{{ isRecording ? '⏹' : '🎤' }}</span>
          <span v-if="isRecording" class="recording-timer">{{ recordingTime }}s</span>
          <span v-else>Голос</span>
        </button>
        <button @click="triggerPhotoUpload" class="action-btn photo-btn">
          <span class="photo-icon">📷</span>
          <span>Фото</span>
        </button>
        <button @click="showMap = !showMap" class="action-btn map-btn">
          <span class="map-icon">{{ showMap ? '✕' : '📍' }}</span>
          <span>{{ showMap ? 'Закрыть' : 'Место' }}</span>
        </button>
      </div>
      <input 
        ref="photoInput" 
        type="file" 
        accept="image/*" 
        capture="environment"
        style="display: none" 
        @change="handlePhotoUpload"
      />
      <div v-if="isRecording" class="recording-indicator">
        <div class="recording-wave">
          <div class="wave-bar"></div>
          <div class="wave-bar"></div>
          <div class="wave-bar"></div>
          <div class="wave-bar"></div>
        </div>
        <span class="recording-text">Идет запись...</span>
      </div>
      <div class="subtitle" v-if="statusMsg" style="margin-top:8px; text-align:center;">{{ statusMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'
import DSCard from '../ui/DSCard.vue'
import DSInput from '../ui/DSInput.vue'
import DSButton from '../ui/DSButton.vue'
import { auth } from '../../store/auth'
import ChatMapPicker from './ChatMapPicker.vue'
import AIResponseCard from './AIResponseCard.vue'

const messages = ref([
  { role: 'assistant', text: 'Здравствуйте! Опишите вашу жалобу — я зафиксирую её.' }
])
const text = ref('')
const statusMsg = ref('')
const list = ref(null)
let mediaRecorder = null
let chunks = []
const isRecording = ref(false)
const lat = ref(null)
const lng = ref(null)
const showMap = ref(false)
const recordingTime = ref(0)
let recordingInterval = null
const photoInput = ref(null)
const selectedPhoto = ref(null)
const photoPreview = ref(null)
const photoText = ref('')

async function send() {
  const t = text.value.trim()
  if (!t) return
  messages.value.push({ role: 'user', text: t })
  text.value = ''
  await nextTick(); scrollBottom()
  try {
    const endpoint = auth.user ? '/api/complaints/submit' : '/api/complaints/chat'
    const payload = { message: t }
    if (lat.value != null && lng.value != null) { payload.lat = lat.value; payload.lng = lng.value }
    // Always get raw response from webhook
    const response = await axios.post(endpoint, payload, { transformResponse: r => r })
    
    console.log('=== CHAT RESPONSE ===')
    console.log('Data:', response.data)
    console.log('Data type:', typeof response.data)
    console.log('Data length:', response.data?.length)
    console.log('====================')
    
    // Check if response is actually empty
    if (!response.data || response.data.trim() === '') {
      console.error('EMPTY RESPONSE FROM SERVER!')
      messages.value.push({ 
        role: 'assistant', 
        text: '[Сервер вернул пустой ответ. Проверьте логи бэкенда и n8n workflow]'
      })
      statusMsg.value = ''
      await nextTick(); scrollBottom()
      return
    }
    
    // Parse JSON and extract data (handle nested output field)
    let displayText = response.data
    let aiData = null
    
    try {
      let parsed = JSON.parse(response.data)
      console.log('Parsed level 1:', parsed)
      
      // Check if there's an "output" field with nested JSON
      if (parsed.output && typeof parsed.output === 'string') {
        try {
          parsed = JSON.parse(parsed.output)
          console.log('Parsed level 2 (from output):', parsed)
        } catch (e) {
          console.log('Output is not JSON, using as is')
        }
      }
      
      // Store full data for AIResponseCard
      aiData = parsed
      
      // Extract text for typing animation
      if (parsed.recommendation_kk) {
        displayText = parsed.recommendation_kk
      } else if (parsed.recommendation) {
        displayText = parsed.recommendation
      } else if (parsed.message) {
        displayText = parsed.message
      } else if (parsed.answer) {
        displayText = parsed.answer
      } else {
        displayText = JSON.stringify(parsed, null, 2)
      }
    } catch (e) {
      console.log('Not JSON, showing raw text:', response.data)
    }
    
    // Add message with typing animation
    const msgIndex = messages.value.length
    messages.value.push({ 
      role: 'assistant', 
      text: '',
      isTyping: true,
      data: aiData
    })
    
    statusMsg.value = ''
    await nextTick()
    scrollBottom()
    
    // Animate typing
    let currentText = ''
    for (let i = 0; i < displayText.length; i++) {
      currentText += displayText[i]
      messages.value[msgIndex].text = currentText
      await new Promise(resolve => setTimeout(resolve, 20)) // 20ms per character
      scrollBottom()
    }
    messages.value[msgIndex].isTyping = false
  } catch (e) {
    messages.value.push({ role: 'assistant', text: 'Не удалось отправить. Попробуйте позже.' })
  }
  await nextTick(); scrollBottom()
}

function scrollBottom() {
  const el = list.value
  if (el) el.scrollTop = el.scrollHeight
}

async function toggleRecord() {
  if (isRecording.value) {
    isRecording.value = false
    if (recordingInterval) {
      clearInterval(recordingInterval)
      recordingInterval = null
    }
    recordingTime.value = 0
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
      mediaRecorder.stop()
      // Stop all tracks
      mediaRecorder.stream.getTracks().forEach(track => track.stop())
    }
    return
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    chunks = []
    
    // Use audio/webm or audio/ogg
    const mimeType = MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : 'audio/ogg'
    mediaRecorder = new MediaRecorder(stream, { mimeType })
    
    mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) chunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      const blob = new Blob(chunks, { type: mimeType })
      const extension = mimeType === 'audio/webm' ? 'webm' : 'ogg'
      const file = new File([blob], `voice.${extension}`, { type: mimeType })
      
      const fd = new FormData()
      fd.append('audio', file, `voice.${extension}`)
      if (text.value) fd.append('text', text.value)
      if (lat.value != null) fd.append('lat', String(lat.value))
      if (lng.value != null) fd.append('lng', String(lng.value))
      
      messages.value.push({ 
        role: 'user', 
        text: `🎤 Голосовое сообщение (${recordingTime.value}s)` 
      })
      await nextTick(); scrollBottom()
      
      statusMsg.value = 'Обработка голосового сообщения...'
      
      try {
        const { data } = await axios.post('/api/complaints/chat-voice', fd, { 
          headers: { 'Content-Type': 'multipart/form-data' },
          transformResponse: r => r,
          timeout: 60000 // 60 seconds timeout
        })
        
        statusMsg.value = ''
        
           // Try to parse as JSON for structured display
           try {
             const parsed = JSON.parse(data)
             if (parsed && typeof parsed === 'object') {
               const responseText = parsed.recommendation_kk || 
                                  parsed.recommendation || 
                                  parsed.message || 
                                  parsed.answer || 
                                  parsed.response ||
                                  'Голосовое сообщение обработано'
               
               messages.value.push({ 
                 role: 'assistant', 
                 text: responseText,
                 data: parsed
               })
             } else {
               messages.value.push({ role: 'assistant', text: data })
             }
           } catch {
             messages.value.push({ role: 'assistant', text: data })
           }
      } catch (e) {
        statusMsg.value = ''
        const errorMsg = e.response?.data || 'Не удалось отправить голосовое сообщение'
        messages.value.push({ role: 'assistant', text: errorMsg })
      }
      await nextTick(); scrollBottom()
    }
    
    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0
    
    // Start timer
    recordingInterval = setInterval(() => {
      recordingTime.value++
      if (recordingTime.value >= 60) { // Max 60 seconds
        toggleRecord()
      }
    }, 1000)
    
  } catch (e) {
    statusMsg.value = 'Микрофон недоступен. Разрешите доступ к микрофону.'
    console.error('Microphone error:', e)
  }
}

function triggerPhotoUpload() {
  photoInput.value?.click()
}

async function handlePhotoUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  // Reset input
  event.target.value = ''
  
  // Save photo and show preview
  selectedPhoto.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    photoPreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

async function sendPhoto() {
  if (!selectedPhoto.value) return
  
  console.log('Sending photo, user:', auth.user)
  
  // Show preview in chat
  messages.value.push({
    role: 'user',
    text: photoText.value || '📷 Фото отправлено',
    image: photoPreview.value
  })
  
  await nextTick(); scrollBottom()
  statusMsg.value = 'Обработка фото...'
  
  try {
    const fd = new FormData()
    fd.append('photo', selectedPhoto.value, selectedPhoto.value.name)
    if (photoText.value) fd.append('text', photoText.value)
    if (lat.value != null) fd.append('lat', String(lat.value))
    if (lng.value != null) fd.append('lng', String(lng.value))
    
    const endpoint = auth.user ? '/api/complaints/submit-photo' : '/api/complaints/chat-photo'
    console.log('Photo endpoint:', endpoint)
    const { data } = await axios.post(endpoint, fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
      transformResponse: r => r,
      timeout: 60000,
      withCredentials: true
    })
    
    statusMsg.value = ''
    photoText.value = ''
    selectedPhoto.value = null
    photoPreview.value = null
    
    // Try to parse as JSON for structured display
    try {
      const parsed = JSON.parse(data)
      if (parsed && typeof parsed === 'object') {
        const responseText = parsed.recommendation_kk || 
                           parsed.recommendation || 
                           parsed.message || 
                           parsed.answer || 
                           parsed.response ||
                           'Фото обработано'
        
        messages.value.push({ 
          role: 'assistant', 
          text: responseText,
          data: parsed
        })
      } else {
        messages.value.push({ role: 'assistant', text: data })
      }
    } catch {
      messages.value.push({ role: 'assistant', text: data })
    }
  } catch (e) {
    statusMsg.value = ''
    const errorMsg = e.response?.data || 'Не удалось отправить фото'
    messages.value.push({ role: 'assistant', text: errorMsg })
  }
  
  await nextTick(); scrollBottom()
}

function cancelPhoto() {
  selectedPhoto.value = null
  photoPreview.value = null
  photoText.value = ''
}
</script>

<style scoped>
.chat-container { 
  display: flex; 
  flex-direction: column; 
  height: 100%; 
  background: white; 
  border-radius: 16px; 
  overflow: hidden;
}

.chat-header { 
  background: linear-gradient(135deg, var(--accent) 0%, #8B5CF6 100%); 
  color: white; 
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.header-content { 
  display: flex; 
  align-items: center; 
  gap: 12px; 
}

.avatar { 
  width: 40px; 
  height: 40px; 
  background: rgba(255,255,255,0.2); 
  border-radius: 50%; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  font-size: 20px;
  backdrop-filter: blur(10px);
}

.messages { 
  flex: 1; 
  overflow-y: auto; 
  padding: 20px; 
  background: #F8FAFC;
  min-height: 300px;
  max-height: 400px;
}

.map-container {
  padding: 12px;
  background: white;
  border-top: 1px solid #E2E8F0;
  max-height: 300px;
  overflow: hidden;
}

.message-wrapper { 
  display: flex; 
  margin-bottom: 16px; 
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-wrapper.user { justify-content: flex-end; }
.message-wrapper.assistant { justify-content: flex-start; }

.message-bubble { 
  max-width: 85%; 
  padding: 12px 16px; 
  border-radius: 16px; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: relative;
}

.ai-content {
  padding: 4px;
}

.message-wrapper.assistant .message-bubble { 
  background: white; 
  border-bottom-left-radius: 4px;
}

.message-wrapper.user .message-bubble { 
  background: linear-gradient(135deg, var(--accent) 0%, #8B5CF6 100%); 
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text { 
  word-wrap: break-word; 
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-image {
  margin-bottom: 8px;
}

.message-image img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 12px;
  object-fit: cover;
}

.input-area { 
  background: white; 
  padding: 16px 20px; 
  border-top: 1px solid #E2E8F0;
}

.input-wrapper { 
  display: flex; 
  gap: 8px; 
  align-items: center;
  background: #F8FAFC;
  border-radius: 24px;
  padding: 8px 12px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: var(--accent);
  background: white;
  box-shadow: 0 0 0 4px rgba(167,139,250,0.1);
}

.chat-input { 
  flex: 1; 
  border: none; 
  background: transparent; 
  outline: none; 
  font-size: 14px; 
  padding: 8px 12px;
  font-family: inherit;
}

.button-group { 
  display: flex; 
  gap: 6px; 
}

.send-btn { 
  width: 36px; 
  height: 36px; 
  border-radius: 50%; 
  border: none; 
  background: linear-gradient(135deg, var(--accent) 0%, #8B5CF6 100%); 
  color: white; 
  cursor: pointer; 
  display: flex; 
  align-items: center; 
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(167,139,250,0.3);
}

.send-btn:hover:not(:disabled) { 
  transform: scale(1.05); 
  box-shadow: 0 4px 12px rgba(167,139,250,0.4);
}

.send-btn:disabled { 
  opacity: 0.5; 
  cursor: not-allowed; 
}

.action-buttons { 
  display: flex; 
  gap: 8px; 
  margin-top: 12px;
  justify-content: center;
}

.action-btn { 
  padding: 8px 16px; 
  border-radius: 20px; 
  border: 1px solid #E2E8F0; 
  background: white; 
  cursor: pointer; 
  font-size: 13px;
  transition: all 0.2s;
  font-family: inherit;
}

.action-btn:hover { 
  background: #F8FAFC; 
  border-color: var(--accent);
  transform: translateY(-1px);
}

.voice-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.voice-icon {
  font-size: 16px;
}

.recording-timer {
  font-weight: 700;
  font-family: monospace;
}

.action-btn.recording { 
  background: #FEE2E2; 
  border-color: #EF4444; 
  color: #DC2626;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.recording-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px;
  background: #FEF2F2;
  border-radius: 12px;
  margin-top: 8px;
}

.recording-wave {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 20px;
}

.wave-bar {
  width: 3px;
  background: #EF4444;
  border-radius: 2px;
  animation: wave 1s ease-in-out infinite;
}

.wave-bar:nth-child(1) { animation-delay: 0s; }
.wave-bar:nth-child(2) { animation-delay: 0.1s; }
.wave-bar:nth-child(3) { animation-delay: 0.2s; }
.wave-bar:nth-child(4) { animation-delay: 0.3s; }

@keyframes wave {
  0%, 100% { height: 8px; }
  50% { height: 20px; }
}

.recording-text {
  color: #DC2626;
  font-weight: 600;
  font-size: 13px;
}

.photo-preview-container {
  padding: 16px;
  background: white;
  border-top: 1px solid #E2E8F0;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.photo-preview-card {
  position: relative;
  background: #F8FAFC;
  border-radius: 16px;
  padding: 16px;
  border: 2px solid var(--accent);
}

.photo-close {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.6);
  color: white;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;
}

.photo-close:hover {
  background: rgba(0,0,0,0.8);
  transform: scale(1.1);
}

.photo-preview-img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 12px;
  margin-bottom: 12px;
}

.photo-text-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #E2E8F0;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.photo-text-input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(167,139,250,0.1);
}

.photo-send-btn {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, var(--accent) 0%, #8B5CF6 100%);
  color: white;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(167,139,250,0.3);
}

.photo-send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(167,139,250,0.4);
}

.photo-btn {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  color: white;
  border: none;
}

.photo-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16,185,129,0.3);
}

.photo-icon {
  font-size: 18px;
}

.map-btn {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  color: white;
  border: none;
}

.map-btn:hover {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59,130,246,0.3);
}

.map-icon {
  font-size: 18px;
}
</style>


