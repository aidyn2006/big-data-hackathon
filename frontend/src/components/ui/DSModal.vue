<template>
  <transition name="fade">
    <div v-if="open" class="overlay" @click.self="$emit('close')">
      <div class="panel glass-card">
        <header>
          <h3>{{ title }}</h3>
          <button class="close" @click="$emit('close')">✕</button>
        </header>
        <div class="content">
          <slot />
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({ open: Boolean, title: String })
defineEmits(['close'])
</script>

<style scoped>
.overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.4); display: grid; place-items: center; padding: 16px; }
.panel { max-width: 560px; width: 100%; }
header { display: flex; align-items: center; justify-content: space-between; padding: 16px; border-bottom: 1px solid rgba(2,8,23,0.06); }
.content { padding: 16px; }
.close { background: transparent; border: 0; font-size: 18px; cursor: pointer; }
</style>


