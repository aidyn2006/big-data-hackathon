<template>
  <div class="ds-input">
    <label v-if="label">{{ label }}</label>
    <input v-bind="$attrs" :value="modelValue" @input="$emit('update:modelValue', $event.target.value)" />
  </div>
  
</template>

<script setup>
defineProps({
  modelValue: String,
  label: String
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.ds-input { display: grid; gap: 8px; }
label { font-size: 12px; color: var(--muted); font-weight: 600; }
input {
  background: white;
  border: 2px solid #E2E8F0;
  border-radius: 12px;
  padding: 12px 14px;
  outline: none;
  transition: border-color .2s;
}
input:focus { border-color: var(--accent); }
</style>


