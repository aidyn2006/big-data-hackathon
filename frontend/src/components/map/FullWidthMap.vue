<template>
  <section class="fullwidth-section">
    <div id="full-map" class="full-map"></div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(async () => {
  const L = (await import('leaflet')).default
  const map = L.map('full-map').setView([43.238949, 76.889709], 11)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(map)
})
</script>

<style scoped>
.fullwidth-section { width: 100vw; height: 70vh; margin-left: 50%; transform: translateX(-50%); }
.full-map { width: 100%; height: 100%; }
@media (max-width: 768px) { .fullwidth-section { height: 60vh; } }
</style>


