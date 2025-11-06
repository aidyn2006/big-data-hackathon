<template>
  <DSCard class="soft-shadow">
    <h3 class="title" style="margin:0 0 12px;">Чат‑агент жалобы</h3>
    <div class="messages glass-card" ref="list">
      <div v-for="(m,i) in messages" :key="i" class="row" :class="m.role">
        <div class="bubble">{{ m.text }}</div>
      </div>
    </div>
    <div class="input-row">
      <DSInput v-model="text" placeholder="Опишите проблему... номер маршрута, место, время" @keyup.enter="send" />
      <DSButton @click="send">Отправить</DSButton>
    </div>
    <div class="subtitle" v-if="statusMsg">{{ statusMsg }}</div>
  </DSCard>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'
import DSCard from '../ui/DSCard.vue'
import DSInput from '../ui/DSInput.vue'
import DSButton from '../ui/DSButton.vue'

const messages = ref([
  { role: 'assistant', text: 'Здравствуйте! Опишите вашу жалобу — я зафиксирую её.' }
])
const text = ref('')
const statusMsg = ref('')
const list = ref(null)

async function send() {
  const t = text.value.trim()
  if (!t) return
  messages.value.push({ role: 'user', text: t })
  text.value = ''
  await nextTick(); scrollBottom()
  try {
    const { data } = await axios.post('/api/complaints/chat', { message: t })
    messages.value.push({ role: 'assistant', text: data.reply })
    statusMsg.value = 'Сообщение отправлено'
  } catch (e) {
    messages.value.push({ role: 'assistant', text: 'Не удалось отправить. Попробуйте позже.' })
  }
  await nextTick(); scrollBottom()
}

function scrollBottom() {
  const el = list.value
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<style scoped>
.messages { max-height: 260px; overflow:auto; padding: 12px; border-radius: 12px; margin-bottom: 12px; }
.row { display: flex; margin: 8px 0; }
.row.user { justify-content: flex-end; }
.row.assistant { justify-content: flex-start; }
.bubble { max-width: 70%; padding: 10px 12px; border-radius: 12px; background: #EEF2FF; }
.row.user .bubble { background: #DBEAFE; }
.input-row { display:flex; gap:8px; align-items:center; margin-top:8px; }
</style>


