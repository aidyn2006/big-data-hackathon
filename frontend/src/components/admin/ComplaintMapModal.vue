<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="open" class="overlay" @click.self="$emit('close')">
        <div class="panel glass-card soft-shadow">
          <header>
            <h3>Карта жалобы</h3>
            <button class="close" @click="$emit('close')">✕</button>
          </header>
          <div class="content">
            <div v-if="!coords" class="subtitle">Координаты не указаны</div>
            <div v-else id="adm-map" class="map"></div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { onMounted, watch, ref } from 'vue'
const props = defineProps({ open: Boolean, complaint: Object })
defineEmits(['close'])
let map, marker
const coords = ref(null)

function init() {
  if (!props.complaint || props.complaint.latitude == null || props.complaint.longitude == null) {
    coords.value = null; return
  }
  coords.value = [props.complaint.latitude, props.complaint.longitude]
  setTimeout(async () => {
    const L = (await import('leaflet')).default
    map = L.map('adm-map').setView(coords.value, 13)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map)
    marker = L.marker(coords.value).addTo(map)
  }, 0)
}

watch(() => props.open, (v) => { if (v) init() })
onMounted(() => { if (props.open) init() })
</script>

<style scoped>
.overlay { position: fixed; inset:0; background: rgba(15,23,42,.5); display:grid; place-items:center; padding:16px; z-index:1000; }
.panel { max-width: 800px; width: 100%; border-radius: var(--radius); }
header { display:flex; align-items:center; justify-content:space-between; padding:16px; border-bottom:1px solid rgba(2,8,23,0.06); }
.content { padding:16px; }
.map { width: 100%; height: 400px; border-radius: 12px; overflow: hidden; }
.close { background: transparent; border: 0; font-size: 20px; cursor: pointer; color: var(--muted); }
</style>




