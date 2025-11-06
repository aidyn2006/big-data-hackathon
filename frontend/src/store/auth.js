import { reactive } from 'vue'
import axios from 'axios'

export const auth = reactive({
  user: null,
  loading: false,
  error: ''
})

export async function checkMe() {
  try {
    const { data } = await axios.get('/api/auth/me')
    auth.user = { username: data.username, authorities: data.authorities || [] }
  } catch {
    auth.user = null
  }
}

export async function login(username, password) {
  auth.loading = true
  auth.error = ''
  try {
    await axios.post('/api/auth/login', { username, password })
    const me = await axios.get('/api/auth/me')
    auth.user = { username: me.data.username, authorities: me.data.authorities || [] }
    return true
  } catch (e) {
    auth.error = e?.response?.data?.error || 'Ошибка входа'
    return false
  } finally {
    auth.loading = false
  }
}

export async function register(username, email, password, role = 'resident') {
  auth.loading = true
  auth.error = ''
  try {
    const roles = role === 'admin' ? 'ROLE_ADMIN' : 'ROLE_USER'
    await axios.post('/api/auth/register', { username, email, password, roles })
    return true
  } catch (e) {
    auth.error = e?.response?.data?.error || 'Ошибка регистрации'
    return false
  } finally {
    auth.loading = false
  }
}

export async function logout() {
  try { await axios.post('/api/auth/logout') } catch {}
  auth.user = null
}


