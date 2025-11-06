<template>
  <section class="ds-card glass-card" :class="[{ padded }]">
    <slot />
  </section>
</template>

<script setup>
defineProps({ padded: { type: Boolean, default: true } })
</script>

<style scoped>
.ds-card { width: 100%; }
.ds-card.padded { padding: 16px; }
</style>


