<template>
  <button class="voice-fab" @click="$emit('open')" title="Голосовой чат с AI">
    🎤
  </button>
</template>

<script setup>
defineEmits(['open'])
</script>

<style scoped>
.voice-fab {
  position: fixed; 
  right: 20px; 
  bottom: 90px;
  width: 56px; 
  height: 56px; 
  border-radius: 999px; 
  border: 0;
  background: linear-gradient(135deg, #2D9A86, #20c997);
  color: #fff; 
  font-size: 24px; 
  cursor: pointer; 
  box-shadow: 0 8px 32px rgba(45, 154, 134, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
}

.voice-fab:hover { 
  transform: translateY(-2px) scale(1.05); 
  box-shadow: 0 12px 40px rgba(45, 154, 134, 0.5);
}

.voice-fab:active {
  transform: translateY(0) scale(0.98);
}
</style>

