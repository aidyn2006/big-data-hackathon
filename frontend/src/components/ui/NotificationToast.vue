<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          :class="['toast', notification.type]"
        >
          <div class="toast-icon">{{ getIcon(notification.type) }}</div>
          <div class="toast-content">
            <div class="toast-title">{{ notification.title }}</div>
            <div v-if="notification.message" class="toast-message">{{ notification.message }}</div>
          </div>
          <button class="toast-close" @click="removeNotification(notification.id)">×</button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'

const notifications = ref([])

function getIcon(type) {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[type] || 'ℹ️'
}

function addNotification(notification) {
  const id = Date.now()
  notifications.value.push({ ...notification, id })
  
  setTimeout(() => {
    removeNotification(id)
  }, notification.duration || 4000)
}

function removeNotification(id) {
  notifications.value = notifications.value.filter(n => n.id !== id)
}

defineExpose({ addNotification })
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border-left: 4px solid;
  backdrop-filter: blur(10px);
  min-width: 320px;
}

.toast.success {
  border-left-color: #10b981;
  background: linear-gradient(135deg, #ffffff, #f0fdf4);
}

.toast.error {
  border-left-color: #ef4444;
  background: linear-gradient(135deg, #ffffff, #fef2f2);
}

.toast.warning {
  border-left-color: #f59e0b;
  background: linear-gradient(135deg, #ffffff, #fffbeb);
}

.toast.info {
  border-left-color: #3b82f6;
  background: linear-gradient(135deg, #ffffff, #eff6ff);
}

.toast-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: 700;
  font-size: 14px;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.toast-message {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #333;
}

.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(400px);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .toast-container {
    left: 20px;
    right: 20px;
    max-width: none;
  }
  
  .toast {
    min-width: auto;
  }
}
</style>

