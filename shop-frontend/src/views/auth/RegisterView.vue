<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const form = ref({ email: '', password: '', firstName: '', lastName: '', phone: '', address: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''; loading.value = true
  try {
    await auth.register(form.value)
    toast.success('Регистрация успешна!')
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Ошибка регистрации'
  } finally { loading.value = false }
}
</script>

<template>
  <div class="max-w-md mx-auto mt-8">
    <div class="card p-8">
      <h1 class="text-2xl font-bold text-center mb-6">Регистрация</h1>

      <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">{{ error }}</div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Имя</label>
            <input v-model="form.firstName" class="input-field" placeholder="Иван" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Фамилия</label>
            <input v-model="form.lastName" class="input-field" placeholder="Петров" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email *</label>
          <input v-model="form.email" type="email" required class="input-field" placeholder="email@example.com" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль *</label>
          <input v-model="form.password" type="password" required minlength="6" class="input-field" placeholder="Минимум 6 символов" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Телефон</label>
          <input v-model="form.phone" type="tel" class="input-field" placeholder="+7 900 123 45 67" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Адрес доставки</label>
          <textarea v-model="form.address" rows="2" class="input-field" placeholder="Город, улица, дом, квартира"></textarea>
        </div>
        <button type="submit" :disabled="loading" class="btn-primary w-full !py-3">
          {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
        </button>
      </form>

      <p class="text-center text-sm text-gray-500 mt-6">
        Уже есть аккаунт? <router-link to="/login" class="text-indigo-600 font-medium hover:underline">Войти</router-link>
      </p>
    </div>
  </div>
</template>
