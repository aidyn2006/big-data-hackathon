<template>
  <DSCard class="soft-shadow">
    <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:16px;">
      <h3 class="title" style="margin:0;">Мои жалобы</h3>
      <DSButton variant="secondary" @click="load">Обновить</DSButton>
    </div>
    
    <div class="complaints-grid">
      <div v-for="group in groupedComplaints" :key="group.key" class="complaint-card">
        <div class="card-header">
          <div class="count-badge">
            <span class="count-number">{{ group.count }}</span>
            <span class="count-label">{{ group.count === 1 ? 'жалоба' : group.count < 5 ? 'жалобы' : 'жалоб' }}</span>
          </div>
          <span class="chip" :class="statusClass(group.status)">{{ group.status }}</span>
        </div>
        
        <div class="card-body">
          <div class="complaint-text">{{ group.text }}</div>
          
          <div v-if="group.route || group.place" class="complaint-meta">
            <span v-if="group.route" class="meta-item">
              <span class="meta-icon">🚌</span>
              <span class="meta-text">{{ group.route }}</span>
            </span>
            <span v-if="group.place" class="meta-item">
              <span class="meta-icon">📍</span>
              <span class="meta-text">{{ group.place }}</span>
            </span>
          </div>
          
          <div v-if="group.priority" class="priority-badge" :class="priorityClass(group.priority)">
            {{ group.priority }}
          </div>
        </div>
        
        <div class="card-footer">
          <span class="date-text">{{ fmt(group.latestDate) }}</span>
          <button v-if="group.count > 1" @click="group.expanded = !group.expanded" class="expand-btn">
            {{ group.expanded ? 'Скрыть' : 'Показать все' }} ({{ group.count }})
          </button>
        </div>
        
        <div v-if="group.expanded" class="expanded-list">
          <div v-for="item in group.items" :key="item.id" class="expanded-item">
            <span class="expanded-date">{{ fmt(item.createdAt) }}</span>
            <span class="chip small" :class="statusClass(item.status)">{{ item.status }}</span>
          </div>
        </div>
      </div>
    </div>
  </DSCard>
</template>

<script setup>
import axios from 'axios'
import DSCard from '../ui/DSCard.vue'
import DSButton from '../ui/DSButton.vue'
import { ref, onMounted, computed } from 'vue'

const items = ref([])

// Group similar complaints
const groupedComplaints = computed(() => {
  const groups = new Map()
  
  for (const item of items.value) {
    // Create a key based on route, place, and similar text
    const normalizedText = normalizeText(item.rawText)
    const key = `${item.route || ''}_${item.place || ''}_${normalizedText.substring(0, 30)}`
    
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        text: item.rawText,
        route: item.route,
        place: item.place,
        priority: item.priority,
        status: item.status,
        latestDate: item.createdAt,
        count: 0,
        items: [],
        expanded: false
      })
    }
    
    const group = groups.get(key)
    group.count++
    group.items.push(item)
    
    // Update to latest date
    if (new Date(item.createdAt) > new Date(group.latestDate)) {
      group.latestDate = item.createdAt
      group.status = item.status
    }
  }
  
  // Convert to array and sort by date (newest first)
  return Array.from(groups.values()).sort((a, b) => 
    new Date(b.latestDate) - new Date(a.latestDate)
  )
})

function normalizeText(text) {
  if (!text) return ''
  // Remove extra spaces, lowercase, remove punctuation for similarity matching
  return text.toLowerCase()
    .replace(/[.,!?;:]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

async function load() {
  const { data } = await axios.get('/api/complaints/mine')
  items.value = data
}

function fmt(s) {
  if (!s) return '—'
  try { 
    return new Date(s).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch { 
    return s 
  }
}

function statusClass(s) {
  if (!s) return 'info'
  const t = s.toLowerCase()
  if (t.includes('new') || t.includes('sent')) return 'info'
  if (t.includes('progress')) return 'warn'
  if (t.includes('resolv') || t.includes('done')) return 'success'
  return 'info'
}

function priorityClass(p) {
  if (!p) return 'priority-low'
  const t = p.toLowerCase()
  if (t.includes('крит') || t.includes('жоғ') || t.includes('выс')) return 'priority-high'
  if (t.includes('орт') || t.includes('сред')) return 'priority-medium'
  return 'priority-low'
}

onMounted(load)
</script>

<style scoped>
.complaints-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.complaint-card {
  background: white;
  border-radius: 16px;
  border: 2px solid #E2E8F0;
  overflow: hidden;
  transition: all 0.3s;
}

.complaint-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e0e7ff 100%);
  border-bottom: 2px solid #E2E8F0;
}

.count-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.count-number {
  font-size: 24px;
  font-weight: 900;
  line-height: 1;
}

.count-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  opacity: 0.9;
  margin-top: 2px;
}

.card-body {
  padding: 16px;
}

.complaint-text {
  font-size: 15px;
  line-height: 1.6;
  color: #1e293b;
  margin-bottom: 12px;
  font-weight: 500;
}

.complaint-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f1f5f9;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
}

.meta-icon {
  font-size: 16px;
}

.priority-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.priority-high {
  background: #fee2e2;
  color: #991b1b;
}

.priority-medium {
  background: #fef3c7;
  color: #92400e;
}

.priority-low {
  background: #e0f2fe;
  color: #075985;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.date-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.expand-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.expand-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.expanded-list {
  padding: 12px 16px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.expanded-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 8px;
  border: 1px solid #e2e8f0;
}

.expanded-item:last-child {
  margin-bottom: 0;
}

.expanded-date {
  font-size: 12px;
  color: #64748b;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.chip.small {
  padding: 4px 8px;
  font-size: 10px;
}

.chip.info {
  background: #e0f2fe;
  color: #075985;
}

.chip.warn {
  background: #fef3c7;
  color: #92400e;
}

.chip.success {
  background: #d1fae5;
  color: #065f46;
}

@media (max-width: 768px) {
  .complaints-grid {
    grid-template-columns: 1fr;
  }
}
</style>



