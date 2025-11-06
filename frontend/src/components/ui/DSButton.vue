<template>
  <button
    class="ds-btn"
    :class="[`variant-${variant}`]"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' }
})
</script>

<style scoped>
.ds-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 0;
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform .15s ease, box-shadow .15s ease, opacity .2s;
}
.ds-btn:hover { transform: translateY(-1px); box-shadow: var(--shadow); }
.ds-btn:active { transform: translateY(0); }

.variant-primary { background: var(--accent); color: white; }
.variant-secondary { background: #E2E8F0; color: var(--text); }
.variant-ghost { background: transparent; color: var(--text); }
</style>


