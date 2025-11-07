<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <div class="avatar">🤖</div>
        <div>
          <h3 class="title" style="margin:0;">AI Помощник для Администратора</h3>
          <div class="subtitle" style="font-size:12px;">{{ props.complaint ? 'Обсуждение конкретной жалобы' : 'Анализ жалоб и рекомендации' }}</div>
        </div>
      </div>
    </div>

    <!-- Complaint Card -->
    <div v-if="props.complaint" class="complaint-card">
      <div class="complaint-header">
        <span class="complaint-id">ID: {{ props.complaint.id?.substring(0, 8) }}</span>
        <span :class="priorityClass(props.complaint.priority)" class="chip">{{ props.complaint.priority || 'нет' }}</span>
      </div>
      <div class="complaint-text">{{ props.complaint.rawText }}</div>
      <div class="complaint-meta">
        <span v-if="props.complaint.route">🚌 {{ props.complaint.route }}</span>
        <span v-if="props.complaint.place">📍 {{ props.complaint.place }}</span>
        <span v-if="props.complaint.actor">👤 {{ props.complaint.actor }}</span>
      </div>
    </div>
    
    <div class="messages" ref="list">
      <div v-for="(m,i) in messages" :key="i" class="message-wrapper" :class="m.role">
        <div class="message-bubble">
          <div v-if="m.role === 'assistant' && m.data" class="ai-content">
            <AIResponseCard :data="m.data" />
          </div>
          <div v-else class="message-text">
            {{ m.text }}
            <span v-if="m.isTyping" class="typing-cursor">|</span>
          </div>
        </div>
      </div>
      
      <!-- Typing indicator -->
      <div v-if="statusMsg" class="message-wrapper assistant">
        <div class="message-bubble typing-indicator-bubble">
          <div class="typing-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="input-area">
      <div class="input-wrapper">
        <input 
          v-model="text" 
          type="text"
          placeholder="Спросите об общей статистике, трендах, рекомендациях..." 
          @keyup.enter="send"
          class="chat-input"
        />
        <div class="button-group">
          <button @click="send" class="send-btn" :disabled="!text.trim()">
            <span>➤</span>
          </button>
        </div>
      </div>
      <div class="subtitle" v-if="statusMsg" style="margin-top:8px; text-align:center;">{{ statusMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, watch } from 'vue'
import axios from 'axios'
import AIResponseCard from './AIResponseCard.vue'

const props = defineProps({
  complaint: Object
})

const messages = ref([])
const text = ref('')
const statusMsg = ref('')
const list = ref(null)

// Initialize messages based on context
watch(() => props.complaint, (newComplaint) => {
  if (newComplaint) {
    messages.value = [
      { role: 'assistant', text: 'Я вижу эту жалобу. Задайте вопрос о ней, попросите анализ или рекомендации по решению.' }
    ]
  } else {
    messages.value = [
      { role: 'assistant', text: 'Здравствуйте! Я AI помощник для анализа жалоб. Задайте мне вопрос о статистике, трендах или попросите рекомендации.' }
    ]
  }
}, { immediate: true })

function priorityClass(p) {
  if (!p) return 'chip info'
  const t = p.toLowerCase()
  if (t.includes('крит') || t.includes('жоғ') || t.includes('выс')) return 'chip danger'
  if (t.includes('орт') || t.includes('сред')) return 'chip warn'
  return 'chip info'
}

async function send() {
  const t = text.value.trim()
  if (!t) return
  messages.value.push({ role: 'user', text: t })
  text.value = ''
  await nextTick(); scrollBottom()
  
  statusMsg.value = 'Анализирую данные...'
  
  try {
    const payload = { message: t }
    if (props.complaint) {
      payload.complaintId = props.complaint.id
    }
    
    const response = await axios.post('/api/complaints/admin-chat', 
      payload, 
      { transformResponse: r => r }
    )
    
    console.log('=== FULL RESPONSE ===')
    console.log('Data:', response.data)
    console.log('Data type:', typeof response.data)
    console.log('Data length:', response.data?.length)
    console.log('Data trimmed:', response.data?.trim())
    console.log('Data is falsy?', !response.data)
    console.log('=====================')
    
    statusMsg.value = ''
    
    // Check if response is actually empty
    if (!response.data || response.data.trim() === '') {
      console.error('EMPTY RESPONSE FROM SERVER!')
      messages.value.push({
        role: 'assistant', 
        text: '[Сервер вернул пустой ответ. Проверьте логи бэкенда и n8n workflow]'
      })
      await nextTick()
      scrollBottom()
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
    
    await nextTick()
    scrollBottom()
    
    // Animate typing with variable speed (faster for spaces, slower for punctuation)
    let currentText = ''
    for (let i = 0; i < displayText.length; i++) {
      currentText += displayText[i]
      messages.value[msgIndex].text = currentText
      
      // Variable typing speed for more natural effect
      const char = displayText[i]
      let delay = 15 // Base speed: 15ms per character
      if (char === ' ') delay = 5 // Fast for spaces
      else if (['.', '!', '?', ','].includes(char)) delay = 200 // Pause at punctuation
      else if (['\n'].includes(char)) delay = 100 // Pause at line breaks
      
      await new Promise(resolve => setTimeout(resolve, delay))
      scrollBottom()
    }
    messages.value[msgIndex].isTyping = false
  } catch (e) {
    statusMsg.value = ''
    console.error('Request failed:', e)
    const errorMsg = e.response?.data?.error || 'Не удалось получить ответ. Попробуйте позже.'
    messages.value.push({ role: 'assistant', text: errorMsg })
  }
  
  await nextTick()
  scrollBottom()
  console.log('Scrolled to bottom')
}

function scrollBottom() {
  const el = list.value
  if (el) el.scrollTop = el.scrollHeight
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
  background: linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%);
  color: white;
  padding: 20px 24px;
  border-bottom: none;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, rgba(255,255,255,0.3), rgba(255,255,255,0.1));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(180deg, #f8f9ff 0%, #ffffff 100%);
  min-height: 300px;
  max-height: 450px;
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
  background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 100%);
  border: 1px solid #c7d2fe;
  border-bottom-left-radius: 4px;
  color: #1e1b4b;
}

.message-wrapper.user .message-bubble {
  background: linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.message-text { 
  word-wrap: break-word; 
  line-height: 1.5;
  white-space: pre-wrap;
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: currentColor;
  margin-left: 2px;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 49% { opacity: 1; }
  50%, 100% { opacity: 0; }
}

.typing-indicator-bubble {
  background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%) !important;
  padding: 16px 20px !important;
  border: 1px solid #c7d2fe;
}

.typing-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8B5CF6;
  animation: bounce 1.4s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% { 
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% { 
    transform: scale(1.2);
    opacity: 1;
  }
}

.input-area { 
  background: linear-gradient(180deg, #ffffff 0%, #f8f9ff 100%);
  padding: 20px 24px; 
  border-top: 1px solid #e0e7ff;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.03);
}

.input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
  background: white;
  border-radius: 28px;
  padding: 10px 16px;
  border: 2px solid #e0e7ff;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.08);
}

.input-wrapper:focus-within {
  border-color: #8B5CF6;
  background: white;
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.15);
  transform: translateY(-1px);
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.complaint-card {
  background: #FEF3C7;
  border-left: 4px solid #F59E0B;
  padding: 16px;
  margin: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.complaint-id {
  font-size: 11px;
  font-weight: 600;
  color: #92400E;
  font-family: monospace;
}

.complaint-text {
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #1F2937;
}

.complaint-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #6B7280;
  margin-top: 8px;
}

.complaint-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.chip.info { background: #E0F2FE; color: #075985; }
.chip.warn { background: #FEF3C7; color: #92400E; }
.chip.danger { background: #FEE2E2; color: #991B1B; }
</style>

