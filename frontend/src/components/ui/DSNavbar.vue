<template>
  <header class="navbar glass-card">
    <div class="nav-inner">
      <div class="brand">
        <div class="dot" />
        <span>BDH Dashboard</span>
      </div>
      <div class="actions">
        <DSButton variant="ghost">Помощь</DSButton>
        <DSButton>Войти</DSButton>
      </div>
    </div>
  </header>
</template>

<script setup>
import DSButton from './DSButton.vue'
</script>

<style scoped>
.navbar { position: sticky; top: 0; margin: 0 0 16px; padding: 12px 16px; }
.nav-inner { display: flex; align-items: center; justify-content: space-between; }
.brand { display: flex; align-items: center; gap: 10px; font-weight: 800; letter-spacing: -0.02em; }
.dot { width: 12px; height: 12px; background: var(--accent); border-radius: 999px; box-shadow: 0 0 0 6px rgba(167,139,250,0.2); }
.actions { display: flex; gap: 8px; }
</style>


