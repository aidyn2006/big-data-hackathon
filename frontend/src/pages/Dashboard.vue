<template>
  <div class="grid" style="gap: 24px;">
    <ChatBox />
    <div class="grid cols-3">
      <DSCard class="soft-shadow">
        <h2 class="title" style="margin:0 0 8px;">Всего жалоб</h2>
        <div style="font-size:36px; font-weight:800;">{{ summary.total ?? 0 }}</div>
        <div class="subtitle">Средний индекс уверенности: <b>{{ (summary.avgConfidence ?? 0).toFixed(2) }}</b></div>
      </DSCard>
      <PriorityPie :byPriority="summary.byPriority || {}" />
      <DSCard class="soft-shadow">
        <h3 class="title" style="margin:0 0 12px;">Импорт</h3>
        <textarea v-model="bulkText" placeholder="Вставьте строки CSV..." style="width:100%; min-height:100px; border-radius:12px; border:2px solid #E2E8F0; padding:12px;"></textarea>
        <div style="display:flex; gap:8px; margin-top:10px;">
          <DSButton @click="importText">Загрузить</DSButton>
          <span class="subtitle" v-if="importStatus">{{ importStatus }}</span>
        </div>
      </DSCard>
    </div>

    <DSCard class="soft-shadow">
      <div style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:8px;">
        <h3 class="title" style="margin:0;">Список жалоб</h3>
        <div style="display:flex; gap:8px;">
          <DSInput v-model="filters.route" placeholder="Маршрут" />
          <DSInput v-model="filters.priority" placeholder="Приоритет" />
          <DSButton variant="secondary" @click="loadComplaints">Фильтр</DSButton>
        </div>
      </div>
      <div class="glass-card" style="overflow:auto; border-radius:12px;">
        <table style="width:100%; border-collapse:separate; border-spacing:0;">
          <thead>
            <tr style="text-align:left;">
              <th style="padding:12px;">Время</th>
              <th style="padding:12px;">Маршрут</th>
              <th style="padding:12px;">Объект</th>
              <th style="padding:12px;">Текст</th>
              <th style="padding:12px;">Приоритет</th>
              <th style="padding:12px; width:160px;">Индекс</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in complaints" :key="c.id" style="border-top:1px solid rgba(2,8,23,0.06);">
              <td style="padding:12px; white-space:nowrap;">{{ fmt(c.time || c.createdAt) }}</td>
              <td style="padding:12px;">{{ c.route }}</td>
              <td style="padding:12px;">{{ c.object }}</td>
              <td style="padding:12px; max-width:420px;">{{ c.rawText }}</td>
              <td style="padding:12px;">
                <span :class="priorityClass(c.priority)" class="chip">{{ c.priority ?? 'нет' }}</span>
              </td>
              <td style="padding:12px;">
                <div style="height:10px; background:#E2E8F0; border-radius:99px; overflow:hidden;">
                  <div :style="{width: `${Math.round((c.confidence || 0)*100)}%`, background: 'linear-gradient(90deg, var(--accent), var(--accent-2))', height: '100%'}"></div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </DSCard>

    <div class="grid cols-3">
      <RouteBar :byRoute="summary.byRoute || {}" />
      <div></div>
      <div></div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import DSCard from '../components/ui/DSCard.vue'
import DSInput from '../components/ui/DSInput.vue'
import DSButton from '../components/ui/DSButton.vue'
import PriorityPie from '../components/charts/PriorityPie.vue'
import RouteBar from '../components/charts/RouteBar.vue'
import ChatBox from '../components/chat/ChatBox.vue'
import { ref, onMounted } from 'vue'

const summary = ref({ byPriority: {}, byRoute: {}, total: 0, avgConfidence: 0 })
const complaints = ref([])
const bulkText = ref('')
const importStatus = ref('')
const filters = ref({ route: '', priority: '' })

async function loadSummary() {
  const { data } = await axios.get('/api/complaints/summary')
  summary.value = data
}

async function loadComplaints() {
  const params = {}
  if (filters.value.route) params.route = filters.value.route
  if (filters.value.priority) params.priority = filters.value.priority
  params.limit = 200
  const { data } = await axios.get('/api/complaints', { params })
  complaints.value = data
}

async function importText() {
  if (!bulkText.value.trim()) return
  const { data } = await axios.post('/api/complaints/bulk-text', bulkText.value, {
    headers: { 'Content-Type': 'text/plain' }
  })
  importStatus.value = `Импортировано: ${data.imported}, пропущено: ${data.skipped}`
  bulkText.value = ''
  await Promise.all([loadSummary(), loadComplaints()])
}

function fmt(s) {
  if (!s) return '—'
  try { return new Date(s).toLocaleString('ru-RU') } catch { return s }
}

function priorityClass(p) {
  if (!p) return 'chip info'
  const t = p.toLowerCase()
  if (t.includes('крит')) return 'chip danger'
  if (t.includes('жоғ') || t.includes('выс')) return 'chip danger'
  if (t.includes('орт') || t.includes('сред')) return 'chip warn'
  return 'chip info'
}

onMounted(() => { loadSummary(); loadComplaints() })
</script>

<style scoped>
/* Additional local tweaks if needed */
</style>


