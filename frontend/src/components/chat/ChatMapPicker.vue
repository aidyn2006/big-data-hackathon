<template>
  <div class="pick-wrap">
    <div id="pick-map" class="map"></div>
    <div class="subtitle" style="margin-top:6px;">{{ lat && lng ? `Место: ${lat.toFixed(5)}, ${lng.toFixed(5)}` : 'Выберите место на карте' }}</div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
const props = defineProps({ lat: Number, lng: Number })
const emit = defineEmits(['update:lat', 'update:lng'])
let map, marker
const lat = ref(props.lat || null)
const lng = ref(props.lng || null)

onMounted(async () => {
  const L = (await import('leaflet')).default
  map = L.map('pick-map').setView([43.238949, 76.889709], 11)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map)
  map.on('click', (e) => {
    const { lat: la, lng: ln } = e.latlng
    if (marker) marker.setLatLng([la, ln])
    else marker = L.marker([la, ln]).addTo(map)
    lat.value = la; lng.value = ln
    emit('update:lat', la); emit('update:lng', ln)
  })
  if (lat.value && lng.value) {
    marker = L.marker([lat.value, lng.value]).addTo(map)
    map.setView([lat.value, lng.value], 13)
  }
})

watch(() => props.lat, v => { lat.value = v })
watch(() => props.lng, v => { lng.value = v })
</script>

<style scoped>
.map { width: 100%; height: 220px; border-radius: 12px; overflow: hidden; }
</style>



