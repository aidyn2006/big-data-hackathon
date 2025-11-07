<template>
  <DSCard class="map-card">
    <h3 class="map-title">🗺️ Жалобалар картасы</h3>
    <div ref="mapContainer" class="map-container"></div>
    
    <div class="map-legend">
      <div class="legend-item">
        <div class="marker-dot critical"></div>
        <span>Өте жоғары</span>
      </div>
      <div class="legend-item">
        <div class="marker-dot high"></div>
        <span>Жоғары</span>
      </div>
      <div class="legend-item">
        <div class="marker-dot medium"></div>
        <span>Орташа</span>
      </div>
      <div class="legend-item">
        <div class="marker-dot low"></div>
        <span>Төмен</span>
      </div>
    </div>
  </DSCard>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import DSCard from '../ui/DSCard.vue'

const props = defineProps({
  complaints: {
    type: Array,
    default: () => []
  }
})

const mapContainer = ref(null)
let map = null
let markers = []

onMounted(() => {
  initMap()
  updateMarkers()
})

watch(() => props.complaints, () => {
  updateMarkers()
}, { deep: true })

function initMap() {
  if (!mapContainer.value) return
  
  // Центр Алматы
  map = L.map(mapContainer.value).setView([43.2380, 76.9450], 12)
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap'
  }).addTo(map)
}

function updateMarkers() {
  if (!map) return
  
  // Очистка старых маркеров
  markers.forEach(marker => marker.remove())
  markers = []
  
  // Группировка по локациям
  const locationMap = {}
  
  props.complaints.forEach(complaint => {
    if (complaint.latitude && complaint.longitude) {
      const key = `${complaint.latitude},${complaint.longitude}`
      if (!locationMap[key]) {
        locationMap[key] = {
          lat: complaint.latitude,
          lng: complaint.longitude,
          complaints: [],
          criticalCount: 0,
          highCount: 0
        }
      }
      locationMap[key].complaints.push(complaint)
      
      const priority = (complaint.priority || '').toLowerCase()
      if (priority.includes('өте жоғары') || priority.includes('критическ')) {
        locationMap[key].criticalCount++
      } else if (priority.includes('жоғары') || priority.includes('высок')) {
        locationMap[key].highCount++
      }
    }
  })
  
  // Создание маркеров
  Object.values(locationMap).forEach(location => {
    const count = location.complaints.length
    const { color, className } = getMarkerStyle(location)
    
    const marker = L.circleMarker([location.lat, location.lng], {
      radius: Math.min(8 + count * 3, 25),
      fillColor: color,
      color: '#fff',
      weight: 3,
      opacity: 1,
      fillOpacity: 0.8,
      className: className
    })
    
    // Popup с информацией
    const popupContent = `
      <div style="font-size: 13px;">
        <strong>${count} жалоба</strong><br>
        ${location.criticalCount > 0 ? `🔴 Өте жоғары: ${location.criticalCount}<br>` : ''}
        ${location.highCount > 0 ? `🟠 Жоғары: ${location.highCount}<br>` : ''}
        <small>${location.complaints[0].place || 'Алматы'}</small>
      </div>
    `
    
    marker.bindPopup(popupContent)
    marker.addTo(map)
    markers.push(marker)
  })
}

function getMarkerStyle(location) {
  if (location.criticalCount > 0) {
    return { color: '#ef4444', className: 'marker-critical' }
  } else if (location.highCount > 0) {
    return { color: '#f59e0b', className: 'marker-high' }
  } else if (location.complaints.length > 3) {
    return { color: '#eab308', className: 'marker-medium' }
  }
  return { color: '#10b981', className: 'marker-low' }
}
</script>

<style scoped>
.map-card {
  background: white;
}

.map-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text);
}

.map-container {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.1);
}

.map-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
}

.marker-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.marker-dot.critical {
  background: #ef4444;
}

.marker-dot.high {
  background: #f59e0b;
}

.marker-dot.medium {
  background: #eab308;
}

.marker-dot.low {
  background: #10b981;
}

@media (max-width: 768px) {
  .map-container {
    height: 300px;
  }
  
  .map-legend {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>

<style>
.marker-critical {
  animation: pulse-critical 2s ease infinite;
}

@keyframes pulse-critical {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}
</style>

