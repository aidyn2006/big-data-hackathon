<template>
  <DSCard class="soft-shadow">
    <h3 class="title" style="margin:0 0 16px;">Частота аспектов</h3>
    <canvas ref="chart"></canvas>
  </DSCard>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import DSCard from '../ui/DSCard.vue'

Chart.register(...registerables)

const props = defineProps({
  byAspect: { type: Object, default: () => ({}) }
})

const chart = ref(null)
let chartInstance = null

function renderChart() {
  if (!chart.value) return
  
  const aspects = Object.entries(props.byAspect)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
  
  const labels = aspects.map(([k]) => k || 'Не указано')
  const data = aspects.map(([, v]) => v)
  
  if (chartInstance) chartInstance.destroy()
  
  chartInstance = new Chart(chart.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Количество жалоб',
        data,
        backgroundColor: 'rgba(167, 139, 250, 0.7)',
        borderColor: 'rgba(167, 139, 250, 1)',
        borderWidth: 2,
        borderRadius: 8
      }]
    },
    options: {
      indexAxis: 'y',
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
        x: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.05)' }
        },
        y: {
          grid: { display: false }
        }
      }
    }
  })
}

onMounted(() => renderChart())
watch(() => props.byAspect, renderChart, { deep: true })
</script>

<style scoped>
canvas { height: 300px !important; }
</style>


