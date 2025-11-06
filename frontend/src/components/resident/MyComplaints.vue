<template>
  <DSCard class="soft-shadow">
    <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
      <h3 class="title" style="margin:0;">Мои жалобы</h3>
      <DSButton variant="secondary" @click="load">Обновить</DSButton>
    </div>
    <div class="glass-card" style="overflow:auto; border-radius:12px;">
      <table style="width:100%; border-collapse:separate; border-spacing:0;">
        <thead>
          <tr style="text-align:left;">
            <th style="padding:12px;">Дата</th>
            <th style="padding:12px;">Статус</th>
            <th style="padding:12px;">Текст</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in items" :key="c.id" style="border-top:1px solid rgba(2,8,23,0.06);">
            <td style="padding:12px; white-space:nowrap;">{{ fmt(c.createdAt) }}</td>
            <td style="padding:12px;"><span class="chip" :class="statusClass(c.status)">{{ c.status }}</span></td>
            <td style="padding:12px; max-width:420px;">{{ c.rawText }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </DSCard>
</template>

<script setup>
import axios from 'axios'
import DSCard from '../ui/DSCard.vue'
import DSButton from '../ui/DSButton.vue'
import { ref, onMounted } from 'vue'

const items = ref([])
async function load() {
  const { data } = await axios.get('/api/complaints/mine')
  items.value = data
}
function fmt(s) {
  if (!s) return '—'
  try { return new Date(s).toLocaleString('ru-RU') } catch { return s }
}
function statusClass(s) {
  if (!s) return 'info'
  const t = s.toLowerCase()
  if (t.includes('new') || t.includes('sent')) return 'info'
  if (t.includes('progress')) return 'warn'
  if (t.includes('resolv')) return 'success'
  return 'info'
}
onMounted(load)
</script>

<style scoped>
</style>



