<template>
  <DSCard padded>
    <h3 class="title" style="margin:0 0 12px;">Карта маршрутов</h3>
    <div id="map" class="map"></div>
    <p class="subtitle" style="margin-top:8px;">Если у записей будут координаты (lat,lng), точки появятся на карте.</p>
  </DSCard>
</template>

<script setup>
import DSCard from '../ui/DSCard.vue'
import { onMounted } from 'vue'

// Lazy init Leaflet only in browser
onMounted(async () => {
  const L = (await import('leaflet')).default
  const map = L.map('map').setView([43.238949, 76.889709], 11) // Almaty center as default
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(map)
})
</script>

<style scoped>
.map { width: 100%; height: 420px; border-radius: 12px; overflow: hidden; }
@media (max-width: 768px) {
  .map { height: 320px; }
}
</style>


