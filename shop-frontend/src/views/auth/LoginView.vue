<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()
const toast = useToast()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

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

      <p class="text-center text-sm text-gray-500 mt-6">
        Нет аккаунта? <router-link to="/register" class="text-indigo-600 font-medium hover:underline">Зарегистрироваться</router-link>
      </p>
    </div>
  </div>
</template>
