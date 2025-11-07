<template>
  <div class="grid" style="gap: 24px;">
    <!-- Admin AI Chat Button -->
    <div v-if="isAdmin" style="display: flex; justify-content: flex-end; margin-bottom: -8px;">
      <DSButton @click="openAdminChat = true" variant="secondary">
        🤖 AI Помощник для анализа
      </DSButton>
    </div>

    <!-- Stats Overview -->
    <div class="grid cols-3">
      <DSCard class="soft-shadow stats-card">
        <div class="stats-icon">📊</div>
        <h2 class="title" style="margin:0 0 8px;">Всего жалоб</h2>
        <div style="font-size:42px; font-weight:800; background: linear-gradient(135deg, var(--accent), var(--accent-2)); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">{{ summary.total ?? 0 }}</div>
        <div class="subtitle">Средний индекс уверенности: <b>{{ (summary.avgConfidence ?? 0).toFixed(2) }}</b></div>
      </DSCard>
      <PriorityStack v-if="isAdmin" :byPriority="summary.byPriority || {}" />
      <DSCard v-if="isAdmin" class="soft-shadow stats-card">
        <div class="stats-icon">🚌</div>
        <h3 class="title" style="margin:0 0 8px;">Маршрутов</h3>
        <div style="font-size:36px; font-weight:800;">{{ Object.keys(summary.byRoute || {}).length }}</div>
        <div class="subtitle">Проблемных участников: <b>{{ Object.keys(summary.byActor || {}).length }}</b></div>
      </DSCard>
    </div>

    <div v-if="isAdmin">
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
                <th style="padding:12px;">Место</th>
                <th style="padding:12px;">Текст</th>
                <th style="padding:12px;">Аспекты</th>
                <th style="padding:12px;">Приоритет</th>
                <th style="padding:12px;">Действия</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in complaints" :key="c.id" style="border-top:1px solid rgba(2,8,23,0.06);">
                <td style="padding:12px; white-space:nowrap;">{{ fmt(c.time || c.createdAt) }}</td>
                <td style="padding:12px;">
                  <span class="chip info" style="background:#E0F2FE; color:#075985;">{{ c.route || '—' }}</span>
                </td>
                <td style="padding:12px;">{{ c.place || '—' }}</td>
                <td style="padding:12px; max-width:320px;">
                  <div style="max-height:60px; overflow:hidden; text-overflow:ellipsis;">{{ c.rawText }}</div>
                </td>
                <td style="padding:12px;">
                  <div style="display:flex; flex-wrap:wrap; gap:4px;">
                    <span v-for="(asp, i) in (c.aspect || [])" :key="i" class="chip" style="font-size:10px; padding:4px 8px; background:#F3F0FF; color:#6B21A8;">
                      {{ asp }}
                    </span>
                    <span v-if="!c.aspect || c.aspect.length === 0" class="subtitle" style="font-size:11px;">—</span>
                  </div>
                </td>
                <td style="padding:12px;">
                  <span :class="priorityClass(c.priority)" class="chip">{{ c.priority ?? 'нет' }}</span>
                </td>
                <td style="padding:12px;">
                  <div style="display:flex; gap:4px;">
                    <DSButton variant="secondary" size="sm" @click="openMap(c)">📍</DSButton>
                    <DSButton variant="secondary" size="sm" @click="discussComplaint(c)">💬</DSButton>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </DSCard>
    </div>

          <ResidentHome v-if="!isAdmin" @openChat="openResidentChat" />
    
    <!-- Analytics Charts for Admin -->
    <div v-if="isAdmin" class="grid cols-2">
      <RouteBar :byRoute="summary.byRoute || {}" />
      <ActorBar :byActor="summary.byActor || {}" />
    </div>
    
    <div v-if="isAdmin">
      <AspectBar :byAspect="summary.byAspect || {}" />
    </div>
  </div>
  <ComplaintMapModal :open="mapModalOpen" :complaint="mapComplaint" @close="mapModalOpen=false" />
  <AdminChatModal :open="openAdminChat" :complaint="selectedComplaint" @close="closeAdminChat" />
  <ChatModal v-if="!isAdmin" :open="openResidentChatModal" @close="openResidentChatModal=false" />
</template>

<script setup>
import axios from 'axios'
import DSCard from '../components/ui/DSCard.vue'
import DSInput from '../components/ui/DSInput.vue'
import DSButton from '../components/ui/DSButton.vue'
import PriorityStack from '../components/charts/PriorityStack.vue'
import RouteBar from '../components/charts/RouteBar.vue'
import ActorBar from '../components/charts/ActorBar.vue'
import AspectBar from '../components/charts/AspectBar.vue'
import { ref, onMounted, computed } from 'vue'
import { auth } from '../store/auth'
import ComplaintMapModal from '../components/admin/ComplaintMapModal.vue'
import MyComplaints from '../components/resident/MyComplaints.vue'
import ResidentHome from '../components/resident/ResidentHome.vue'
import AdminChatModal from '../components/ui/AdminChatModal.vue'
import ChatModal from '../components/ui/ChatModal.vue'

const summary = ref({ byPriority: {}, byRoute: {}, total: 0, avgConfidence: 0 })
const complaints = ref([])
const filters = ref({ route: '', priority: '' })
const isAdmin = computed(() => auth.user?.authorities?.some(a => a.authority === 'ROLE_ADMIN'))
const mapModalOpen = ref(false)
const mapComplaint = ref(null)
const openAdminChat = ref(false)
const selectedComplaint = ref(null)
const openResidentChatModal = ref(false)

function openMap(c) { mapComplaint.value = c; mapModalOpen.value = true }
function discussComplaint(c) {
  selectedComplaint.value = c
  openAdminChat.value = true
}
function openResidentChat() {
  openResidentChatModal.value = true
}

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

function closeAdminChat() {
  openAdminChat.value = false
  selectedComplaint.value = null
}

onMounted(async () => { 
  try {
    await loadSummary()
    if (isAdmin.value) {
      await loadComplaints()
    }
  } catch (e) {
    console.error('Failed to load data:', e)
  }
})
</script>

<style scoped>
.stats-card {
  position: relative;
  overflow: hidden;
}

.stats-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 48px;
  opacity: 0.15;
}

.grid.cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 768px) {
  .grid.cols-2 {
    grid-template-columns: 1fr;
  }
}
</style>


