<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="open" class="overlay" @click.self="$emit('close')">
        <div class="panel glass-card soft-shadow">
          <header>
            <h3>{{ mode === 'login' ? 'Войти' : 'Регистрация' }}</h3>
            <button class="close" @click="$emit('close')">✕</button>
          </header>
          <div class="content">
            <div class="form">
              <DSInput v-model="username" :label="mode==='login' ? 'Email или имя пользователя' : 'Имя пользователя'" :placeholder="mode==='login' ? 'email или username' : 'username'" />
              <DSInput v-if="mode==='register'" v-model="email" label="Email" placeholder="email@example.com" />
              <DSInput v-model="password" type="password" label="Пароль" placeholder="••••••" />
              <div v-if="mode==='register'" class="role-selector">
                <label class="subtitle" style="margin-bottom:6px; display:block;">Роль:</label>
                <div style="display:flex; gap:16px;">
                  <label style="display:flex; align-items:center; gap:6px; cursor:pointer;">
                    <input type="radio" v-model="userRole" value="resident" />
                    <span>Житель</span>
                  </label>
                  <label style="display:flex; align-items:center; gap:6px; cursor:pointer;">
                    <input type="radio" v-model="userRole" value="admin" />
                    <span>Админ</span>
                  </label>
                </div>
              </div>
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
  </teleport>
  
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
const userRole = ref('resident')
const loading = computed(() => auth.loading)
const error = computed(() => auth.error)

async function submit() {
  if (mode.value === 'login') {
    const ok = await login(username.value, password.value)
    if (ok) emitClose()
  } else {
    const ok = await registerApi(username.value, email.value, password.value, userRole.value)
    if (ok) { mode.value = 'login' }
  }
}

function emitClose() { 
  username.value = email.value = password.value = ''
  userRole.value = 'resident'
  auth.error = ''
  emit('close')
}
</script>

<style scoped>
.overlay { position: fixed; inset:0; background: rgba(15,23,42,.5); display:grid; place-items:center; padding: 16px; z-index: 1000; backdrop-filter: blur(4px); }
.panel { max-width: 460px; width: 100%; border-radius: var(--radius); box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
header { display:flex; align-items:center; justify-content:space-between; padding:20px 24px; border-bottom:1px solid rgba(2,8,23,0.06); }
header h3 { margin: 0; font-size: 20px; font-weight: 700; }
.content { padding:24px; }
.form { display:grid; gap:16px; }
.role-selector { padding: 12px; background: #F8FAFC; border-radius: 12px; }
.toggle { margin-top: 8px; text-align: center; }
.toggle a { color: var(--accent); cursor:pointer; text-decoration: underline; }
.close { background: transparent; border: 0; font-size: 24px; cursor: pointer; color: var(--muted); transition: color 0.2s; padding: 4px 8px; }
.close:hover { color: var(--text); }
</style>


