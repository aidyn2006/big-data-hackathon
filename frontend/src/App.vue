<template>
  <div>
    <DSNavbar />
    <main class="container">
      <Dashboard />
    </main>
  </div>
  
</template>

<script setup>
import DSNavbar from './components/ui/DSNavbar.vue'
import Dashboard from './pages/Dashboard.vue'
</script>

<style>
/* global styles in theme.css */
</style>


