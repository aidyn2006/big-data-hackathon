<template>
  <div class="ai-response">
    <div v-if="data.recommendation" class="recommendation-card">
      <div class="recommendation-header">
        <span class="icon">💡</span>
        <span class="label">Ұсыныс / Рекомендация</span>
      </div>
      <p class="recommendation-text">{{ data.recommendation }}</p>
    </div>
    
    <div class="data-grid">
      <div v-if="data.priority" class="data-item priority-item">
        <div class="data-label">Приоритет</div>
        <span :class="getPriorityClass(data.priority)" class="chip">{{ data.priority }}</span>
      </div>
      
      <div v-if="data.route" class="data-item">
        <div class="data-label">Маршрут</div>
        <div class="data-value">🚌 {{ data.route }}</div>
      </div>
      
      <div v-if="data.place" class="data-item">
        <div class="data-label">Место</div>
        <div class="data-value">📍 {{ data.place }}</div>
      </div>
      
      <div v-if="data.actor" class="data-item">
        <div class="data-label">Участник</div>
        <div class="data-value">👤 {{ data.actor }}</div>
      </div>
      
      <div v-if="data.time" class="data-item">
        <div class="data-label">Время</div>
        <div class="data-value">🕐 {{ formatTime(data.time) }}</div>
      </div>
      
      <div v-if="data.confidence" class="data-item">
        <div class="data-label">Уверенность</div>
        <div class="confidence-bar">
          <div class="confidence-fill" :style="{ width: `${data.confidence * 100}%` }"></div>
          <span class="confidence-text">{{ Math.round(data.confidence * 100) }}%</span>
        </div>
      </div>
    </div>
    
    <div v-if="data.aspects && data.aspects.length > 0" class="aspects-section">
      <div class="data-label">Аспекты жалобы</div>
      <div class="aspects-list">
        <span v-for="(aspect, i) in data.aspects" :key="i" class="aspect-chip">
          {{ aspect }}
        </span>
      </div>
    </div>
    
    <div v-if="data.object" class="object-section">
      <div class="data-label">Объект</div>
      <div class="data-value">{{ data.object }}</div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  data: { type: Object, required: true }
})

function getPriorityClass(priority) {
  if (!priority) return 'chip info'
  const p = priority.toLowerCase()
  if (p.includes('крит') || p.includes('критикалық')) return 'chip danger'
  if (p.includes('жоғ') || p.includes('выс')) return 'chip danger'
  if (p.includes('орт') || p.includes('сред')) return 'chip warn'
  return 'chip info'
}

function formatTime(time) {
  if (!time) return '—'
  try {
    return new Date(time).toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return time
  }
}
</script>

<style scoped>
.ai-response {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-card {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-radius: 12px;
  padding: 12px;
  border-left: 4px solid #F59E0B;
}

.recommendation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #92400E;
  margin-bottom: 8px;
  font-size: 13px;
}

.recommendation-text {
  margin: 0;
  color: #78350F;
  line-height: 1.5;
  font-size: 14px;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.data-item {
  background: #F8FAFC;
  border-radius: 10px;
  padding: 10px;
  border: 1px solid #E2E8F0;
}

.priority-item {
  grid-column: span 2;
}

.data-label {
  font-size: 11px;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
  font-weight: 600;
}

.data-value {
  font-size: 14px;
  color: #0F172A;
  font-weight: 500;
}

.confidence-bar {
  position: relative;
  height: 24px;
  background: #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
}

.confidence-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #10B981, #059669);
  transition: width 0.5s ease;
}

.confidence-text {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 700;
  color: #0F172A;
  z-index: 1;
}

.aspects-section {
  background: #F3F0FF;
  border-radius: 10px;
  padding: 12px;
  border: 1px solid #E9D5FF;
}

.aspects-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.aspect-chip {
  background: white;
  color: #6B21A8;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #E9D5FF;
}

.object-section {
  background: #EFF6FF;
  border-radius: 10px;
  padding: 12px;
  border: 1px solid #DBEAFE;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.chip.danger {
  background: #FEE2E2;
  color: #991B1B;
}

.chip.warn {
  background: #FEF3C7;
  color: #92400E;
}

.chip.info {
  background: #E0F2FE;
  color: #075985;
}
</style>

