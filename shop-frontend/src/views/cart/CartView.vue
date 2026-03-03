<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const toast = useToast()

const comment = ref('')
const checkingOut = ref(false)

const canCheckout = computed(() => {
  const balance = auth.user?.balance ?? 0
  return cart.cart.items.length > 0 && balance >= cart.totalAmount
})

const insufficientBalance = computed(() => {
  const balance = auth.user?.balance ?? 0
  return cart.cart.items.length > 0 && balance < cart.totalAmount
})

function formatPrice(n: number) { return n.toLocaleString('ru-RU') + ' ₽' }

function imgSrc(url: string, name: string) {
  return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=6366f1&color=fff&size=200`
}

async function checkout() {
  checkingOut.value = true
  try {
    const { data } = await ordersApi.checkout(comment.value || undefined)
    toast.success(`Заказ ${data.data.orderNumber} оформлен!`)
    await auth.fetchMe() // refresh balance
    await cart.fetchCart() // should be empty now
    router.push(`/orders/${data.data.id}`)
  } catch (e: any) {
    toast.error(e.response?.data?.message || 'Ошибка оформления заказа')
  } finally { checkingOut.value = false }
}

onMounted(() => cart.fetchCart())
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <h1 class="text-2xl font-bold mb-6">Корзина</h1>

    <!-- Empty -->
    <div v-if="cart.cart.items.length === 0" class="card p-12 text-center">
      <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z"/>
      </svg>
      <h2 class="text-lg font-medium text-gray-600 mb-2">Корзина пуста</h2>
      <p class="text-gray-400 mb-4">Добавьте товары из каталога</p>
      <router-link to="/" class="btn-primary">Перейти в каталог</router-link>
    </div>

    <!-- Cart items -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-3">
        <div v-for="item in cart.cart.items" :key="item.productId"
          class="card p-4 flex gap-4 items-center">
          <img :src="imgSrc(item.productImageUrl, item.productName)" :alt="item.productName"
            class="w-20 h-20 rounded-lg object-cover bg-gray-100 flex-shrink-0 cursor-pointer"
            @click="router.push(`/product/${item.productId}`)" />

          <div class="flex-1 min-w-0">
            <h3 class="font-medium text-gray-900 truncate cursor-pointer hover:text-indigo-600"
              @click="router.push(`/product/${item.productId}`)">{{ item.productName }}</h3>
            <p class="text-sm text-gray-400">{{ item.productSku }} · {{ formatPrice(item.price) }} / {{ item.unit }}</p>
          </div>

          <!-- Quantity -->
          <div class="flex items-center gap-2 flex-shrink-0">
            <button @click="cart.updateItem(item.productId, item.quantity - 1)"
              class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:bg-gray-100">−</button>
            <span class="w-8 text-center font-semibold">{{ item.quantity }}</span>
            <button @click="cart.updateItem(item.productId, item.quantity + 1)"
              class="w-8 h-8 rounded-full bg-indigo-600 text-white flex items-center justify-center hover:bg-indigo-700">+</button>
          </div>

          <!-- Line total -->
          <div class="text-right flex-shrink-0 w-28">
            <div class="font-bold">{{ formatPrice(item.totalPrice) }}</div>
          </div>

          <!-- Remove -->
          <button @click="cart.removeItem(item.productId)"
            class="text-gray-400 hover:text-red-500 transition-colors flex-shrink-0">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Summary -->
      <div class="lg:col-span-1">
        <div class="card p-6 sticky top-24">
          <h3 class="font-bold text-lg mb-4">Итого</h3>
          <div class="space-y-2 mb-4">
            <div class="flex justify-between text-sm text-gray-600">
              <span>Товары ({{ cart.cart.items.length }})</span>
              <span>{{ formatPrice(cart.totalAmount) }}</span>
            </div>
            <div class="flex justify-between text-sm text-gray-600">
              <span>Ваш баланс</span>
              <span :class="insufficientBalance ? 'text-red-600 font-semibold' : 'text-green-600'">
                {{ formatPrice(auth.user?.balance ?? 0) }}
              </span>
            </div>
          </div>
          <div class="border-t border-gray-100 pt-4 mb-4">
            <div class="flex justify-between font-bold text-lg">
              <span>К оплате</span>
              <span>{{ formatPrice(cart.totalAmount) }}</span>
            </div>
          </div>

          <div v-if="insufficientBalance" class="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-sm mb-4">
            Недостаточно средств на балансе. Пополните баланс для оформления заказа.
          </div>

          <textarea v-model="comment" rows="2" class="input-field mb-4" placeholder="Комментарий к заказу (необязательно)"></textarea>

          <button @click="checkout" :disabled="!canCheckout || checkingOut" class="btn-primary w-full !py-3 text-base">
            {{ checkingOut ? 'Оформление...' : 'Оформить заказ' }}
          </button>

          <button @click="cart.clearCart" class="w-full text-sm text-gray-500 hover:text-red-500 mt-3 transition-colors">
            Очистить корзину
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
