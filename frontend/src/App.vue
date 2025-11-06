<template>
  <div>
    <DSNavbar />
    <main class="container">
      <Dashboard />
    </main>
    <ChatFab @open="openChat=true" />
    <ChatModal :open="openChat" @close="openChat=false" />
  </div>
  
</template>

<script setup>
import DSNavbar from './components/ui/DSNavbar.vue'
import Dashboard from './pages/Dashboard.vue'
import ChatFab from './components/ui/ChatFab.vue'
import ChatModal from './components/ui/ChatModal.vue'
import { ref } from 'vue'

const openChat = ref(false)
</script>

<style>
/* global styles in theme.css */
</style>


