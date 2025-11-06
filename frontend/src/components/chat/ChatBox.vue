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
        <button @click="toggleRecord" class="action-btn" :class="{recording: isRecording}">
          {{ isRecording ? '⏹ Стоп' : '🎤 Голос' }}
        </button>
        <button @click="showMap = !showMap" class="action-btn">
          {{ showMap ? '✕ Закрыть карту' : '📍 Выбрать место' }}
        </button>
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
    const { data } = await axios.post(endpoint, payload, { transformResponse: r => r })
    
    // Try to parse as JSON for structured display
    try {
      const parsed = JSON.parse(data)
      if (parsed && typeof parsed === 'object') {
        messages.value.push({ 
          role: 'assistant', 
          text: parsed.recommendation || parsed.message || 'Жалоба обработана',
          data: parsed
        })
      } else {
        messages.value.push({ role: 'assistant', text: data })
      }
    } catch {
      // Not JSON, show as text
      messages.value.push({ role: 'assistant', text: data })
    }
    statusMsg.value = ''
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
    mediaRecorder.stop()
    return
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    chunks = []
    mediaRecorder = new MediaRecorder(stream)
    mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) chunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      const blob = new Blob(chunks, { type: 'audio/webm' })
      const file = new File([blob], 'voice.webm', { type: 'audio/webm' })
      const fd = new FormData()
      fd.append('file', file)
      if (text.value) fd.append('text', text.value)
      if (lat.value != null) fd.append('lat', String(lat.value))
      if (lng.value != null) fd.append('lng', String(lng.value))
      messages.value.push({ role: 'user', text: '[Голосовое сообщение отправлено]' })
      await nextTick(); scrollBottom()
      try {
        const { data } = await axios.post('/api/complaints/chat-voice', fd, { headers: { 'Content-Type': 'multipart/form-data' }, transformResponse: r => r })
        const replyText = typeof data === 'string' ? data : JSON.stringify(data)
        messages.value.push({ role: 'assistant', text: replyText })
      } catch (e) {
        messages.value.push({ role: 'assistant', text: 'Не удалось отправить голосовое.' })
      }
      await nextTick(); scrollBottom()
    }
    mediaRecorder.start()
    isRecording.value = true
  } catch (e) {
    statusMsg.value = 'Микрофон недоступен'
  }
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
</style>


