<template>
  <DSCard>
    <h3 class="title" style="margin:0 0 12px;">Маршруты</h3>
    <Bar :data="chartData" :options="options" />
  </DSCard>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from 'chart.js'
import DSCard from '../ui/DSCard.vue'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const props = defineProps({ byRoute: { type: Object, default: () => ({}) } })

const chartData = computed(() => {
  const labels = Object.keys(props.byRoute)
  const data = Object.values(props.byRoute)
  return {
    labels,
    datasets: [{ label: 'Кол-во', data, backgroundColor: 'rgba(167,139,250,0.6)', borderRadius: 8 }]
  }
})

const options = { plugins: { legend: { display: false }}, responsive: true, maintainAspectRatio: false }
</script>

<style scoped>
:deep(canvas){ width:100% !important; height:240px !important; }
</style>


