<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()

const cartCount = computed(() => cart.itemCount)

async function handleLogout() {
  await auth.logout()
  router.push('/')
}
</script>

<template>
  <header class="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2">
          <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
          </div>
          <span class="text-xl font-bold text-gray-900">IT Shop</span>
        </router-link>

        <!-- Navigation -->
        <nav class="flex items-center gap-4">
          <router-link to="/" class="text-gray-600 hover:text-indigo-600 font-medium transition-colors">
            Каталог
          </router-link>

          <template v-if="auth.isAuthenticated">
            <router-link to="/orders" class="text-gray-600 hover:text-indigo-600 font-medium transition-colors">
              Заказы
            </router-link>

            <!-- Cart button -->
            <router-link to="/cart" class="relative p-2 text-gray-600 hover:text-indigo-600 transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z"/>
              </svg>
              <span v-if="cartCount > 0"
                class="absolute -top-1 -right-1 bg-indigo-600 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
                {{ cartCount > 9 ? '9+' : cartCount }}
              </span>
            </router-link>

            <!-- Balance -->
            <span class="text-sm text-gray-500 hidden sm:block">
              {{ (auth.user?.balance ?? 0).toLocaleString('ru-RU') }} ₽
            </span>

            <!-- User menu -->
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-700 hidden sm:block">
                {{ auth.user?.firstName || auth.user?.email }}
              </span>
              <button @click="handleLogout" class="text-gray-500 hover:text-red-600 transition-colors text-sm">
                Выйти
              </button>
            </div>
          </template>

          <template v-else>
            <router-link to="/login" class="btn-secondary text-sm">Войти</router-link>
            <router-link to="/register" class="btn-primary text-sm">Регистрация</router-link>
          </template>
        </nav>
      </div>
    </div>
  </header>
</template>
