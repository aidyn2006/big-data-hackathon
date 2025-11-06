<template>
  <transition name="fade">
    <div v-if="open" class="overlay" @click.self="$emit('close')">
      <div class="panel glass-card soft-shadow">
        <header>
          <h3>{{ mode === 'login' ? 'Войти' : 'Регистрация' }}</h3>
          <button class="close" @click="$emit('close')">✕</button>
        </header>
        <div class="content">
          <div class="form">
            <DSInput v-model="username" label="Имя пользователя" placeholder="username" />
            <DSInput v-if="mode==='register'" v-model="email" label="Email" placeholder="email" />
            <DSInput v-model="password" type="password" label="Пароль" placeholder="••••••" />
            <DSButton :disabled="loading" @click="submit">
              {{ loading ? '...' : (mode==='login' ? 'Войти' : 'Зарегистрироваться') }}
            </DSButton>
            <div class="subtitle" v-if="error" style="color:#b91c1c;">{{ error }}</div>
            <div class="toggle subtitle">
              <span v-if="mode==='login'">Нет аккаунта?
                <a @click="mode='register'">Зарегистрироваться</a></span>
              <span v-else>Уже есть аккаунт? <a @click="mode='login'">Войти</a></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import DSButton from './DSButton.vue'
import DSInput from './DSInput.vue'
import { auth, login, register as registerApi } from '../../store/auth'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['close'])

const mode = ref('login')
const username = ref('')
const email = ref('')
const password = ref('')
const loading = computed(() => auth.loading)
const error = computed(() => auth.error)

async function submit() {
  if (mode.value === 'login') {
    const ok = await login(username.value, password.value)
    if (ok) emitClose()
  } else {
    const ok = await registerApi(username.value, email.value, password.value)
    if (ok) { mode.value = 'login' }
  }
}

function emitClose() { 
  username.value = email.value = password.value = ''
  auth.error = ''
  emit('close')
}
</script>

<style scoped>
.overlay { position: fixed; inset:0; background: rgba(15,23,42,.5); display:grid; place-items:center; padding: 16px; z-index: 1000; }
.panel { max-width: 420px; width: 100%; border-radius: var(--radius); }
header { display:flex; align-items:center; justify-content:space-between; padding:16px; border-bottom:1px solid rgba(2,8,23,0.06); }
.content { padding:16px; }
.form { display:grid; gap:12px; }
.toggle a { color: var(--accent); cursor:pointer; }
.close { background: transparent; border: 0; font-size: 20px; cursor: pointer; color: var(--muted); }
</style>


