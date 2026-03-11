import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { UserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isAuthenticated = computed(() => !!user.value)
  const token = ref(localStorage.getItem('shop_accessToken') || '')

  async function login(email: string, password: string) {
    const { data } = await authApi.login({ email, password })
    setAuth(data.data)
  }

  async function register(form: any) {
    const { data } = await authApi.register(form)
    setAuth(data.data)
  }

  function setAuth(resp: any) {
    token.value = resp.accessToken
    localStorage.setItem('shop_accessToken', resp.accessToken)
    localStorage.setItem('shop_refreshToken', resp.refreshToken)
    user.value = resp.user
  }

  async function fetchMe() {
    try {
      const { data } = await authApi.me()
      user.value = data.data
    } catch { logout() }
  }

  async function logout() {
    try { await authApi.logout() } catch {}
    user.value = null; token.value = ''
    localStorage.removeItem('shop_accessToken')
    localStorage.removeItem('shop_refreshToken')
  }

  async function init() {
    if (localStorage.getItem('shop_accessToken')) await fetchMe()
  }

  return { user, isAuthenticated, token, login, register, logout, fetchMe, init, setAuth }
})
