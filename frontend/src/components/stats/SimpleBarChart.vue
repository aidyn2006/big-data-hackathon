<template>
  <DSCard class="bar-chart-card">
    <h3 class="chart-title">📊 {{ title }}</h3>
    <div class="chart-container">
      <div 
        v-for="(item, index) in chartData" 
        :key="index"
        class="bar-item"
      >
        <div class="bar-label">{{ item.label }}</div>
        <div class="bar-wrapper">
          <div 
            class="bar"
            :class="item.priority"
            :style="{ width: item.percentage + '%' }"
          >
            <span class="bar-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>
  </DSCard>
</template>

<script setup>
import { computed } from 'vue'
import DSCard from '../ui/DSCard.vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  data: {
    type: Object,
    default: () => ({})
  },
  limit: {
    type: Number,
    default: 5
  }
})

const chartData = computed(() => {
  const entries = Object.entries(props.data)
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, props.limit)
  
  const maxValue = Math.max(...entries.map(e => e.value), 1)
  
  return entries.map(item => ({
    label: item.label,
    value: item.value,
    percentage: (item.value / maxValue) * 100,
    priority: getPriorityClass(item.value, maxValue)
  }))
})

function getPriorityClass(value, max) {
  const ratio = value / max
  if (ratio > 0.7) return 'critical'
  if (ratio > 0.4) return 'high'
  return 'medium'
}
</script>

<style scoped>
.bar-chart-card {
  background: white;
}

.chart-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text);
}

.chart-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  min-width: 120px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  text-align: right;
}

.bar-wrapper {
  flex: 1;
  height: 36px;
  background: #f1f5f9;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.bar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 12px;
  border-radius: 8px;
  transition: width 0.6s ease;
  animation: slideIn 0.8s ease forwards;
}

@keyframes slideIn {
  from {
    width: 0 !important;
  }
}

.bar.critical {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.bar.high {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.bar.medium {
  background: linear-gradient(135deg, #10b981, #059669);
}

.bar-value {
  color: white;
  font-weight: 700;
  font-size: 14px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

@media (max-width: 768px) {
  .bar-label {
    min-width: 80px;
    font-size: 12px;
  }
  
  .bar-wrapper {
    height: 30px;
  }
}
</style>

