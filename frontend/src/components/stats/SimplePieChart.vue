<template>
  <DSCard class="pie-chart-card">
    <h3 class="chart-title">🥧 {{ title }}</h3>
    <div class="pie-container">
      <svg :width="size" :height="size" class="pie-svg">
        <g :transform="`translate(${size/2}, ${size/2})`">
          <path
            v-for="(slice, index) in slices"
            :key="index"
            :d="slice.path"
            :fill="slice.color"
            :class="['pie-slice', { 'slice-hover': hoveredIndex === index }]"
            @mouseenter="hoveredIndex = index"
            @mouseleave="hoveredIndex = null"
          >
            <title>{{ slice.label }}: {{ slice.value }} ({{ slice.percentage }}%)</title>
          </path>
        </g>
      </svg>
      
      <div class="pie-legend">
        <div 
          v-for="(slice, index) in slices" 
          :key="index"
          class="legend-item"
          :class="{ 'legend-hover': hoveredIndex === index }"
          @mouseenter="hoveredIndex = index"
          @mouseleave="hoveredIndex = null"
        >
          <div class="legend-color" :style="{ background: slice.color }"></div>
          <div class="legend-content">
            <div class="legend-label">{{ slice.label }}</div>
            <div class="legend-value">{{ slice.value }} ({{ slice.percentage }}%)</div>
          </div>
        </div>
      </div>
    </div>
  </DSCard>
</template>

<script setup>
import { ref, computed } from 'vue'
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
  size: {
    type: Number,
    default: 200
  }
})

const hoveredIndex = ref(null)

const colors = {
  'Өте жоғары': '#ef4444',
  'Критический': '#ef4444',
  'Жоғары': '#f59e0b',
  'Высокий': '#f59e0b',
  'Орташа': '#eab308',
  'Средний': '#eab308',
  'Төмен': '#10b981',
  'Низкий': '#10b981'
}

const slices = computed(() => {
  const entries = Object.entries(props.data)
    .filter(([_, value]) => value > 0)
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
  
  const total = entries.reduce((sum, item) => sum + item.value, 0)
  
  if (total === 0) return []
  
  let currentAngle = -Math.PI / 2 // Start from top
  
  return entries.map((item, index) => {
    const percentage = Math.round((item.value / total) * 100)
    const angle = (item.value / total) * 2 * Math.PI
    const endAngle = currentAngle + angle
    
    const path = createArcPath(
      0, 0,
      props.size / 2 - 20,
      currentAngle,
      endAngle
    )
    
    currentAngle = endAngle
    
    return {
      label: item.label,
      value: item.value,
      percentage,
      path,
      color: colors[item.label] || getColorByIndex(index)
    }
  })
})

function createArcPath(x, y, radius, startAngle, endAngle) {
  const start = polarToCartesian(x, y, radius, endAngle)
  const end = polarToCartesian(x, y, radius, startAngle)
  const largeArc = endAngle - startAngle <= Math.PI ? 0 : 1
  
  return [
    'M', x, y,
    'L', start.x, start.y,
    'A', radius, radius, 0, largeArc, 0, end.x, end.y,
    'Z'
  ].join(' ')
}

function polarToCartesian(centerX, centerY, radius, angle) {
  return {
    x: centerX + radius * Math.cos(angle),
    y: centerY + radius * Math.sin(angle)
  }
}

function getColorByIndex(index) {
  const defaultColors = ['#8b5cf6', '#ec4899', '#06b6d4', '#84cc16']
  return defaultColors[index % defaultColors.length]
}
</script>

<style scoped>
.pie-chart-card {
  background: white;
}

.chart-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text);
}

.pie-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.pie-svg {
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.1));
}

.pie-slice {
  transition: all 0.3s ease;
  cursor: pointer;
}

.pie-slice.slice-hover {
  opacity: 0.8;
  filter: brightness(1.1);
  transform: scale(1.05);
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}

.legend-item:hover,
.legend-item.legend-hover {
  background: #f8f9fa;
  transform: translateX(4px);
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.legend-content {
  flex: 1;
}

.legend-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 2px;
}

.legend-value {
  font-size: 12px;
  color: var(--muted);
  font-weight: 600;
}

@media (max-width: 768px) {
  .pie-container {
    gap: 16px;
  }
}
</style>

