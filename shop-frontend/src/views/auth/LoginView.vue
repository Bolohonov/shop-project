<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToast } from '@/composables/useToast'
import { authApi } from '@/api/auth'

const router = useRouter()
const route  = useRoute()
const auth   = useAuthStore()
const cart   = useCartStore()
const toast  = useToast()

const email       = ref('')
const password    = ref('')
const loading     = ref(false)
const demoLoading = ref(false)
const error       = ref('')

async function handleLogin() {
  error.value = ''; loading.value = true
  try {
    await auth.login(email.value, password.value)
    await cart.fetchCart()
    toast.success('Добро пожаловать!')
    router.push((route.query.redirect as string) || '/')
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Ошибка авторизации'
  } finally { loading.value = false }
}

async function handleDemoLogin() {
  error.value = ''; demoLoading.value = true
  try {
    const { data } = await authApi.demoLogin()
    auth.setAuth(data.data)
    await cart.fetchCart()
    toast.success('Добро пожаловать в демо!')
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Демо временно недоступно'
  } finally { demoLoading.value = false }
}
</script>

<template>
  <div class="max-w-md mx-auto mt-12">
    <div class="card p-8">
      <h1 class="text-2xl font-bold text-center mb-6">Вход в аккаунт</h1>

      <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
        {{ error }}
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="email" type="email" required class="input-field" placeholder="email@example.com" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
          <input v-model="password" type="password" required class="input-field" placeholder="••••••••" />
        </div>
        <button type="submit" :disabled="loading" class="btn-primary w-full !py-3">
          {{ loading ? 'Вход...' : 'Войти' }}
        </button>
      </form>

      <!-- Разделитель -->
      <div class="flex items-center gap-3 my-5">
        <div class="flex-1 h-px bg-gray-200"></div>
        <span class="text-sm text-gray-400">или</span>
        <div class="flex-1 h-px bg-gray-200"></div>
      </div>

      <!-- Демо кнопка -->
      <button
          @click="handleDemoLogin"
          :disabled="demoLoading"
          class="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-lg border-2 border-indigo-300 text-indigo-600 bg-indigo-50 hover:bg-indigo-100 transition-colors font-medium text-sm disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <svg v-if="!demoLoading" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
          <path d="M8 5v14l11-7z"/>
        </svg>
        <svg v-else class="w-4 h-4 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
        </svg>
        {{ demoLoading ? 'Загрузка...' : 'Попробовать демо' }}
        <span class="text-gray-400 font-normal text-xs">· данные сбрасываются каждую ночь</span>
      </button>

      <p class="text-center text-sm text-gray-500 mt-6">
        Нет аккаунта? <router-link to="/register" class="text-indigo-600 font-medium hover:underline">Зарегистрироваться</router-link>
      </p>
    </div>
  </div>
</template>
