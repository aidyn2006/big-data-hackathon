<template>
  <DSCard>
    <h3 class="title" style="margin:0 0 12px;">Приоритеты</h3>
    <Pie :data="chartData" :options="options" />
  </DSCard>
</template>

<script setup>
import { computed } from 'vue'
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js'
import DSCard from '../ui/DSCard.vue'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({ byPriority: { type: Object, default: () => ({}) } })

const chartData = computed(() => {
  const labels = Object.keys(props.byPriority)
  const data = Object.values(props.byPriority)
  const colors = ['#A78BFA','#60A5FA','#34D399','#F472B6','#FBBF24']
  return {
    labels,
    datasets: [{ data, backgroundColor: labels.map((_,i)=>colors[i%colors.length]), borderWidth: 0 }]
  }
})

const options = { plugins: { legend: { position: 'bottom' }}, responsive: true, maintainAspectRatio: false }
</script>

<style scoped>
:deep(canvas){ width:100% !important; height:240px !important; }
</style>


