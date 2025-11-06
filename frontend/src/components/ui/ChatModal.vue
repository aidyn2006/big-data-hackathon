<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="open" class="overlay" @click.self="$emit('close')">
        <div class="panel glass-card soft-shadow">
          <header>
            <h3>Чат</h3>
            <button class="close" @click="$emit('close')">✕</button>
          </header>
          <div class="content">
            <ChatBox />
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import ChatBox from '../chat/ChatBox.vue'
defineProps({ open: Boolean })
defineEmits(['close'])
</script>

<style scoped>
.overlay { 
  position: fixed; 
  inset:0; 
  background: rgba(15,23,42,.6); 
  display:grid; 
  place-items:center; 
  padding:16px; 
  z-index:1000; 
  backdrop-filter: blur(4px);
}

.panel { 
  max-width: 900px; 
  width: 100%; 
  max-height: 85vh; 
  border-radius: 20px; 
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0,0,0,0.3);
}

header { 
  display:flex; 
  align-items:center; 
  justify-content:space-between; 
  padding:16px 20px; 
  background: white;
  border-bottom:1px solid rgba(2,8,23,0.06); 
}

header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.content { 
  padding: 0; 
  height: calc(85vh - 60px); 
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.close { 
  background: transparent; 
  border: 0; 
  font-size: 24px; 
  cursor: pointer; 
  color: var(--muted); 
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 8px;
}

.close:hover { 
  color: var(--text); 
  background: #F8FAFC;
}
</style>


