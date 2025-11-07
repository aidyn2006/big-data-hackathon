<template>
  <DSCard class="timeline-card">
    <div class="timeline-header">
      <div class="header-left">
        <h3 class="timeline-title">⏱️ Жалобалар уақыт желісі</h3>
        <div class="timeline-subtitle">Соңғы 24 сағат ішіндегі белсенділік</div>
      </div>
      <div class="timeline-controls">
        <button 
          v-for="period in periods" 
          :key="period.value"
          @click="selectedPeriod = period.value"
          :class="['period-btn', { active: selectedPeriod === period.value }]"
        >
          {{ period.label }}
        </button>
      </div>
    </div>

    <div class="timeline-container">
      <div class="timeline-line"></div>
      
      <TransitionGroup name="timeline-item">
        <div 
          v-for="(item, index) in timelineItems" 
          :key="item.id"
          :class="['timeline-item', item.priority]"
          :style="{ '--delay': index * 0.1 + 's' }"
        >
          <div class="item-dot"></div>
          <div class="item-connector"></div>
          
          <div class="item-card">
            <div class="item-time">
              <span class="time-icon">🕐</span>
              <span class="time-text">{{ formatTime(item.time) }}</span>
              <span class="time-ago">{{ getTimeAgo(item.time) }}</span>
            </div>
            
            <div class="item-content">
              <div class="item-header">
                <div class="item-route">
                  <span class="route-icon">🚌</span>
                  <span class="route-text">Маршрут {{ item.route }}</span>
                </div>
                <div :class="['item-priority', item.priority]">
                  {{ item.priorityLabel }}
                </div>
              </div>
              
              <div class="item-text">{{ item.text }}</div>
              
              <div class="item-footer">
                <div class="item-location">
                  <span class="location-icon">📍</span>
                  <span>{{ item.place }}</span>
                </div>
                <button @click="viewDetails(item)" class="view-btn">
                  Толығырақ →
                </button>
              </div>
            </div>
          </div>
        </div>
      </TransitionGroup>
      
      <div v-if="timelineItems.length === 0" class="timeline-empty">
        <div class="empty-icon">📭</div>
        <div class="empty-text">Осы кезеңде жалобалар жоқ</div>
      </div>
    </div>
    
    <!-- Pagination -->
    <div v-if="totalPages > 1" class="timeline-pagination">
      <button 
        @click="currentPage--" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        ← Алдыңғы
      </button>
      
      <div class="page-info">
        <span class="current-page">{{ currentPage }}</span>
        <span class="page-sep">/</span>
        <span class="total-page">{{ totalPages }}</span>
      </div>
      
      <button 
        @click="currentPage++" 
        :disabled="currentPage >= totalPages"
        class="page-btn"
      >
        Келесі →
      </button>
    </div>
  </DSCard>
</template>

<script setup>
import { ref, computed } from 'vue'
import DSCard from '../ui/DSCard.vue'

const props = defineProps({
  complaints: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['view-details'])

const selectedPeriod = ref('24h')
const currentPage = ref(1)
const itemsPerPage = 5

const periods = [
  { value: '1h', label: '1 сағат' },
  { value: '6h', label: '6 сағат' },
  { value: '24h', label: '24 сағат' },
  { value: '7d', label: '7 күн' }
]

const filteredComplaints = computed(() => {
  const now = new Date()
  return props.complaints
    .filter(c => {
      const createdAt = new Date(c.createdAt || c.time)
      const hoursDiff = (now - createdAt) / (1000 * 60 * 60)
      
      switch (selectedPeriod.value) {
        case '1h': return hoursDiff <= 1
        case '6h': return hoursDiff <= 6
        case '24h': return hoursDiff <= 24
        case '7d': return hoursDiff <= 168
        default: return true
      }
    })
    .sort((a, b) => {
      const dateA = new Date(a.createdAt || a.time)
      const dateB = new Date(b.createdAt || b.time)
      return dateB - dateA
    })
})

const totalPages = computed(() => {
  return Math.ceil(filteredComplaints.value.length / itemsPerPage)
})

const timelineItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  
  return filteredComplaints.value
    .slice(start, end)
    .map(c => ({
      id: c.id,
      route: c.route,
      text: c.rawText?.substring(0, 120) + (c.rawText?.length > 120 ? '...' : ''),
      place: c.place,
      time: c.createdAt || c.time,
      priority: getPriorityClass(c.priority),
      priorityLabel: c.priority || 'Орташа'
    }))
})

function getPriorityClass(priority) {
  if (!priority) return 'medium'
  const p = priority.toLowerCase()
  if (p.includes('өте жоғары') || p.includes('критическ')) return 'critical'
  if (p.includes('жоғары') || p.includes('высок')) return 'high'
  if (p.includes('орташа') || p.includes('средн')) return 'medium'
  return 'low'
}

function formatTime(time) {
  return new Date(time).toLocaleTimeString('kk-KZ', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

function getTimeAgo(time) {
  const now = new Date()
  const past = new Date(time)
  const diffMs = now - past
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  
  if (diffMins < 1) return 'дәл қазір'
  if (diffMins < 60) return `${diffMins} минут бұрын`
  if (diffHours < 24) return `${diffHours} сағат бұрын`
  return `${diffDays} күн бұрын`
}

function viewDetails(item) {
  emit('view-details', item)
}
</script>

<style scoped>
.timeline-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.timeline-title {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text);
}

.timeline-subtitle {
  font-size: 13px;
  color: var(--muted);
  font-weight: 600;
}

.timeline-controls {
  display: flex;
  gap: 8px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
}

.period-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.period-btn:hover {
  background: rgba(255, 255, 255, 0.5);
}

.period-btn.active {
  background: white;
  color: var(--accent);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.timeline-container {
  position: relative;
  padding-left: 40px;
}

.timeline-line {
  position: absolute;
  left: 15px;
  top: 20px;
  bottom: 20px;
  width: 3px;
  background: linear-gradient(180deg, var(--accent) 0%, #e2e8f0 100%);
  border-radius: 2px;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  animation: slideInTimeline 0.6s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

@keyframes slideInTimeline {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.item-dot {
  position: absolute;
  left: -34px;
  top: 24px;
  width: 14px;
  height: 14px;
  background: white;
  border: 3px solid var(--accent);
  border-radius: 50%;
  z-index: 2;
  box-shadow: 0 0 0 4px rgba(167, 139, 250, 0.2);
}

.timeline-item.critical .item-dot {
  border-color: #ef4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.2);
  animation: pulse-dot 2s ease infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.2);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(239, 68, 68, 0.1);
  }
}

.item-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.item-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: var(--accent);
}

.timeline-item.critical .item-card {
  border-left: 4px solid #ef4444;
}

.timeline-item.high .item-card {
  border-left: 4px solid #f59e0b;
}

.timeline-item.medium .item-card {
  border-left: 4px solid #eab308;
}

.timeline-item.low .item-card {
  border-left: 4px solid #10b981;
}

.item-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--muted);
}

.time-icon {
  font-size: 16px;
}

.time-text {
  font-weight: 700;
  color: var(--text);
}

.time-ago {
  margin-left: auto;
  font-weight: 600;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-route {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--text);
}

.route-icon {
  font-size: 18px;
}

.item-priority {
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
}

.item-priority.critical {
  background: #fee;
  color: #c33;
}

.item-priority.high {
  background: #fef3e2;
  color: #d97706;
}

.item-priority.medium {
  background: #fefce8;
  color: #ca8a04;
}

.item-priority.low {
  background: #f0fdf4;
  color: #15803d;
}

.item-text {
  color: var(--text);
  line-height: 1.6;
  margin-bottom: 12px;
  font-size: 14px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.item-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
  font-weight: 600;
}

.location-icon {
  font-size: 14px;
}

.view-btn {
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.view-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(167, 139, 250, 0.4);
}

.timeline-empty {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: var(--muted);
  font-weight: 600;
}

.timeline-item-enter-active,
.timeline-item-leave-active {
  transition: all 0.3s ease;
}

.timeline-item-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.timeline-item-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.timeline-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  color: var(--text);
}

.page-btn:hover:not(:disabled) {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
  transform: translateY(-1px);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
}

.current-page {
  color: var(--accent);
  font-size: 18px;
}

.page-sep {
  color: var(--muted);
  font-size: 14px;
}

.total-page {
  color: var(--text);
  font-size: 16px;
}

@media (max-width: 768px) {
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .timeline-controls {
    width: 100%;
    overflow-x: auto;
  }
  
  .period-btn {
    white-space: nowrap;
  }
}
</style>

