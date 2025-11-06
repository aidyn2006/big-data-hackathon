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
          <div v-else class="message-text">{{ m.text }}</div>
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
    
    const { data } = await axios.post('/api/complaints/admin-chat', 
      payload, 
      { transformResponse: r => r }
    )
    
    console.log('Admin chat response:', data)
    statusMsg.value = ''
    
    // Try to parse as JSON for structured display
    try {
      const parsed = JSON.parse(data)
      console.log('Parsed response:', parsed)
      
      if (parsed && typeof parsed === 'object') {
        // Extract text from various possible fields
        const responseText = parsed.recommendation_kk || 
                           parsed.recommendation || 
                           parsed.message || 
                           parsed.answer || 
                           parsed.response ||
                           JSON.stringify(parsed, null, 2)
        
        console.log('Response text:', responseText)
        
        const newMessage = {
          role: 'assistant',
          text: responseText,
          data: parsed
        }
        console.log('Adding message:', newMessage)
        messages.value.push(newMessage)
        console.log('Total messages:', messages.value.length)
      } else {
        messages.value.push({ role: 'assistant', text: data })
      }
    } catch (e) {
      // Not JSON, show as text
      console.error('Failed to parse response:', e)
      messages.value.push({ role: 'assistant', text: data })
    }
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
  background: linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%);
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
  border-color: #8B5CF6;
  background: white;
  box-shadow: 0 0 0 4px rgba(139,92,246,0.1);
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
  background: linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(139,92,246,0.3);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(139,92,246,0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

