<template>
  <div>
    <DSNavbar />
    <main class="container">
      <Dashboard />
    </main>
    <FullWidthMap />
  </div>
  
</template>

<script setup>
import DSNavbar from './components/ui/DSNavbar.vue'
import Dashboard from './pages/Dashboard.vue'
import FullWidthMap from './components/map/FullWidthMap.vue'
</script>

<style>
/* global styles in theme.css */
</style>


