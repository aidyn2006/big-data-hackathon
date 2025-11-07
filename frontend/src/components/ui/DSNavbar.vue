<template>
  <header class="navbar glass-card">
    <div class="nav-inner">
      <div class="brand">
        <div class="brand-icon">🚌</div>
        <div class="brand-text">
          <div class="brand-title">Көлік Жүйесі</div>
          <div class="brand-subtitle">Мониторинг Панелі</div>
        </div>
      </div>
      <div class="actions">
        <span v-if="auth.user" class="user-badge">
          <span class="user-icon">👤</span>
          <span class="username">{{ auth.user.username }}</span>
        </span>
        <DSButton v-if="!auth.user" @click="openAuth=true">Кіру</DSButton>
        <DSButton v-else variant="secondary" @click="logout">Шығу</DSButton>
      </div>
    </div>
    <AuthModal :open="openAuth" @close="openAuth=false" />
    <ChatModal :open="openChat" @close="openChat=false" />
  </header>
</template>

<script setup>
import DSButton from './DSButton.vue'
import AuthModal from './AuthModal.vue'
import { ref, onMounted } from 'vue'
import { auth, checkMe, logout } from '../../store/auth'
import ChatModal from './ChatModal.vue'

const openAuth = ref(false)
const openChat = ref(false)
onMounted(() => checkMe())
</script>

<style scoped>
.navbar { 
  position: sticky; 
  top: 0; 
  margin: 0 0 16px; 
  padding: 16px 24px; 
  z-index: 100;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.9));
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.nav-inner { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
}

.brand { 
  display: flex; 
  align-items: center; 
  gap: 14px; 
  font-weight: 700; 
}

.brand-icon {
  font-size: 36px;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.1));
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-title {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
}

.brand-subtitle {
  font-size: 11px;
  color: var(--muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.actions { 
  display: flex; 
  gap: 12px; 
  align-items: center; 
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #F3F0FF, #E9E3FF);
  border-radius: 20px;
  font-weight: 600;
  color: var(--text);
  font-size: 14px;
}

.user-icon {
  font-size: 16px;
}

.username {
  color: var(--accent);
}

@media (max-width: 768px) {
  .brand-subtitle {
    display: none;
  }
  
  .user-badge {
    padding: 6px 12px;
  }
}
</style>


