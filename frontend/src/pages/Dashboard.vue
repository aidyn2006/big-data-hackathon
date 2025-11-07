<template>
  <div class="grid" style="gap: 24px;">
    <!-- Admin AI Chat Button -->
    <div v-if="isAdmin" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <DSButton @click="openAdminChat = true" variant="secondary">
        🤖 AI талдау көмекшісі
      </DSButton>
      <button @click="toggleTheme" class="theme-toggle">
        {{ isDark ? '☀️' : '🌙' }}
      </button>
    </div>

    <!-- Timeline Widget -->
    <ComplaintsTimeline v-if="isAdmin" :complaints="complaints" @view-details="viewComplaintDetails" />

    <div v-if="isAdmin">
      <DSCard class="soft-shadow">
        <div style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:16px;">
          <h3 class="title" style="margin:0;">Жалобалар тізімі ({{ complaints.length }})</h3>
          <div style="display:flex; gap:8px;">
            <DSInput v-model="filters.route" placeholder="Маршрут" />
            <DSInput v-model="filters.priority" placeholder="Басымдық" />
            <DSButton variant="secondary" @click="loadComplaints">Сүзгі</DSButton>
          </div>
        </div>
        <div class="glass-card" style="overflow:auto; border-radius:12px;">
          <table style="width:100%; border-collapse:separate; border-spacing:0;">
            <thead>
              <tr style="text-align:left;">
                <th style="padding:12px;">Уақыт</th>
                <th style="padding:12px;">Маршрут</th>
                <th style="padding:12px;">Орын</th>
                <th style="padding:12px;">Мәтін</th>
                <th style="padding:12px;">Аспектілер</th>
                <th style="padding:12px;">Басымдық</th>
                <th style="padding:12px;">Әрекеттер</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in paginatedComplaints" :key="c.id" style="border-top:1px solid rgba(2,8,23,0.06);">
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
                  <span :class="priorityClass(c.priority)" class="chip">{{ c.priority ?? 'жоқ' }}</span>
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
        
        <!-- Pagination -->
        <div class="pagination">
          <button 
            @click="currentPage--" 
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            ← Алдыңғы
          </button>
          
          <div class="pagination-info">
            <span class="page-number">{{ currentPage }}</span>
            <span class="page-separator">/</span>
            <span class="total-pages">{{ totalPages }}</span>
          </div>
          
          <button 
            @click="currentPage++" 
            :disabled="currentPage >= totalPages"
            class="pagination-btn"
          >
            Келесі →
          </button>
        </div>
      </DSCard>
    </div>

          <ResidentHome v-if="!isAdmin" @openChat="openResidentChat" />
    
    <!-- Simple Statistics -->
    <div v-if="isAdmin" class="grid cols-2">
      <SimpleBarChart title="Маршруттар бойынша" :data="summary.byRoute || {}" />
      <SimplePieChart title="Басымдық бойынша" :data="summary.byPriority || {}" />
    </div>
    
    <div v-if="isAdmin" class="grid cols-2">
      <SimpleBarChart title="Актерлер бойынша" :data="summary.byActor || {}" :limit="5" />
      <SimpleBarChart title="Аспектілер бойынша" :data="summary.byAspect || {}" :limit="6" />
    </div>
    
    <!-- Complaints Map -->
    <SimpleComplaintsMap v-if="isAdmin" :complaints="complaints" />
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
import SimpleBarChart from '../components/stats/SimpleBarChart.vue'
import SimplePieChart from '../components/stats/SimplePieChart.vue'
import SimpleComplaintsMap from '../components/map/SimpleComplaintsMap.vue'
import { ref, onMounted, computed } from 'vue'
import { auth } from '../store/auth'
import ComplaintMapModal from '../components/admin/ComplaintMapModal.vue'
import MyComplaints from '../components/resident/MyComplaints.vue'
import ResidentHome from '../components/resident/ResidentHome.vue'
import AdminChatModal from '../components/ui/AdminChatModal.vue'
import ChatModal from '../components/ui/ChatModal.vue'
import ComplaintsTimeline from '../components/timeline/ComplaintsTimeline.vue'

const summary = ref({ byPriority: {}, byRoute: {}, total: 0, avgConfidence: 0 })
const complaints = ref([])
const filters = ref({ route: '', priority: '' })
const isAdmin = computed(() => auth.user?.authorities?.some(a => a.authority === 'ROLE_ADMIN'))
const mapModalOpen = ref(false)
const mapComplaint = ref(null)
const openAdminChat = ref(false)
const selectedComplaint = ref(null)
const openResidentChatModal = ref(false)
const isDark = ref(false)
const currentPage = ref(1)
const itemsPerPage = ref(10)

const paginatedComplaints = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return complaints.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(complaints.value.length / itemsPerPage.value)
})

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

function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

function viewComplaintDetails(item) {
  const complaint = complaints.value.find(c => c.id === item.id)
  if (complaint) {
    discussComplaint(complaint)
  }
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

.theme-toggle {
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.theme-toggle:hover {
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.pagination-btn {
  padding: 10px 20px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  color: var(--text);
}

.pagination-btn:hover:not(:disabled) {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(167, 139, 250, 0.3);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border-radius: 10px;
  font-weight: 700;
  font-size: 16px;
}

.page-number {
  color: var(--accent);
  font-size: 20px;
}

.page-separator {
  color: var(--muted);
  font-size: 14px;
}

.total-pages {
  color: var(--text);
}

.grid.cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid.cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 968px) {
  .grid.cols-3 {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .grid.cols-2 {
    grid-template-columns: 1fr;
  }
}
</style>


