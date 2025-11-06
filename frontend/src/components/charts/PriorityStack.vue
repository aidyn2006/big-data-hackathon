<template>
  <DSCard class="soft-shadow">
    <h3 class="title" style="margin:0 0 16px;">Распределение по уровням</h3>
    <div class="priority-bars">
      <div v-for="(count, priority) in sortedPriorities" :key="priority" class="priority-item">
        <div class="priority-label">
          <span :class="getPriorityClass(priority)" class="chip">{{ priority }}</span>
          <span class="count">{{ count }}</span>
        </div>
        <div class="bar-container">
          <div 
            class="bar" 
            :class="getPriorityBarClass(priority)"
            :style="{ width: `${(count / total * 100)}%` }"
          ></div>
        </div>
      </div>
    </div>
    <div class="subtitle" style="margin-top:12px; text-align:center;">
      Всего: <b>{{ total }}</b> жалоб
    </div>
  </DSCard>
</template>

<script setup>
import { computed } from 'vue'
import DSCard from '../ui/DSCard.vue'

const props = defineProps({
  byPriority: { type: Object, default: () => ({}) }
})

const priorityOrder = ['критикалық', 'критический', 'жоғары', 'высокий', 'орташа', 'средний', 'төмен', 'низкий']

const sortedPriorities = computed(() => {
  const entries = Object.entries(props.byPriority)
  return Object.fromEntries(
    entries.sort((a, b) => {
      const aIdx = priorityOrder.findIndex(p => a[0]?.toLowerCase().includes(p))
      const bIdx = priorityOrder.findIndex(p => b[0]?.toLowerCase().includes(p))
      if (aIdx === -1 && bIdx === -1) return 0
      if (aIdx === -1) return 1
      if (bIdx === -1) return -1
      return aIdx - bIdx
    })
  )
})

const total = computed(() => Object.values(props.byPriority).reduce((a, b) => a + b, 0))

function getPriorityClass(p) {
  if (!p) return 'chip info'
  const t = p.toLowerCase()
  if (t.includes('крит')) return 'chip danger'
  if (t.includes('жоғ') || t.includes('выс')) return 'chip danger'
  if (t.includes('орт') || t.includes('сред')) return 'chip warn'
  return 'chip info'
}

function getPriorityBarClass(p) {
  if (!p) return 'bar-info'
  const t = p.toLowerCase()
  if (t.includes('крит')) return 'bar-danger'
  if (t.includes('жоғ') || t.includes('выс')) return 'bar-danger'
  if (t.includes('орт') || t.includes('сред')) return 'bar-warn'
  return 'bar-info'
}
</script>

<style scoped>
.priority-bars {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.priority-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.priority-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count {
  font-weight: 700;
  font-size: 18px;
  color: var(--text);
}

.bar-container {
  height: 24px;
  background: #F1F5F9;
  border-radius: 12px;
  overflow: hidden;
}

.bar {
  height: 100%;
  transition: width 0.5s ease;
  border-radius: 12px;
}

.bar-danger {
  background: linear-gradient(90deg, #EF4444, #DC2626);
}

.bar-warn {
  background: linear-gradient(90deg, #F59E0B, #D97706);
}

.bar-info {
  background: linear-gradient(90deg, #3B82F6, #2563EB);
}
</style>

