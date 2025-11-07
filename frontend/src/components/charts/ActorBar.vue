<template>
  <DSCard class="soft-shadow">
    <h3 class="title" style="margin:0 0 16px;">Проблемные участники</h3>
    <canvas ref="chart"></canvas>
  </DSCard>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import DSCard from '../ui/DSCard.vue'

Chart.register(...registerables)

const props = defineProps({
  byActor: { type: Object, default: () => ({}) }
})

const chart = ref(null)
let chartInstance = null

function renderChart() {
  if (!chart.value) return
  
  const actors = Object.entries(props.byActor)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
  
  const labels = actors.map(([k]) => k || 'Не указано')
  const data = actors.map(([, v]) => v)
  
  if (chartInstance) chartInstance.destroy()
  
  chartInstance = new Chart(chart.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Количество жалоб',
        data,
        backgroundColor: 'rgba(239, 68, 68, 0.7)',
        borderColor: 'rgba(239, 68, 68, 1)',
        borderWidth: 2,
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(15, 23, 42, 0.9)',
          padding: 12,
          cornerRadius: 8
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.05)' }
        },
        x: {
          grid: { display: false }
        }
      }
    }
  })
}

onMounted(() => renderChart())
watch(() => props.byActor, renderChart, { deep: true })
</script>

<style scoped>
canvas { height: 280px !important; }
</style>


