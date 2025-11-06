<template>
  <header class="navbar glass-card">
    <div class="nav-inner">
      <div class="brand">
        <div class="dot" />
        <span>BDH Dashboard</span>
      </div>
      <div class="actions">
        <span v-if="auth.user" class="subtitle">{{ auth.user.username }}</span>
        <DSButton v-if="!auth.user" @click="openAuth=true">Войти</DSButton>
        <DSButton v-else variant="secondary" @click="logout">Выйти</DSButton>
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
.navbar { position: sticky; top: 0; margin: 0 0 16px; padding: 12px 16px; z-index: 100; }
.nav-inner { display: flex; align-items: center; justify-content: space-between; }
.brand { display: flex; align-items: center; gap: 10px; font-weight: 800; letter-spacing: -0.02em; }
.dot { width: 12px; height: 12px; background: var(--accent); border-radius: 999px; box-shadow: 0 0 0 6px rgba(167,139,250,0.2); }
.actions { display: flex; gap: 8px; align-items: center; }
</style>


