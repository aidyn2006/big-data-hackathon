<template>
  <button class="fab" @click="$emit('open')">💬</button>
</template>

<script setup>
defineEmits(['open'])
</script>

<style scoped>
.fab {
  position: fixed; right: 20px; bottom: 20px;
  width: 56px; height: 56px; border-radius: 999px; border: 0;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: #fff; font-size: 24px; cursor: pointer; box-shadow: var(--shadow);
}
.fab:hover { transform: translateY(-1px); }
</style>




