<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productsApi } from '@/api/products'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToast } from '@/composables/useToast'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = useToast()

const product = ref<Product | null>(null)
const loading = ref(true)
const quantity = ref(1)

const inCart = computed(() => product.value ? cart.isInCart(product.value.id) : false)
const cartQty = computed(() => product.value ? cart.getQuantity(product.value.id) : 0)
const imgSrc = computed(() => product.value?.imageUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(product.value?.name || 'P')}&background=6366f1&color=fff&size=600`)

function formatPrice(price: number) { return price.toLocaleString('ru-RU') + ' ₽' }

async function addToCart() {
  if (!auth.isAuthenticated) { router.push('/login'); return }
  if (!product.value) return
  try {
    await cart.addItem(product.value.id, quantity.value)
    toast.success(`${product.value.name} добавлен в корзину (${quantity.value} шт.)`)
  } catch (e: any) { toast.error(e.response?.data?.message || 'Ошибка') }
}

onMounted(async () => {
  try {
    const { data } = await productsApi.getById(route.params.id as string)
    product.value = data.data
  } catch { router.push('/') }
  finally { loading.value = false }
})
</script>

<template>
  <div v-if="loading" class="flex justify-center py-20">
    <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
  </div>

  <div v-else-if="product" class="max-w-5xl mx-auto">
    <!-- Breadcrumb -->
    <nav class="flex items-center gap-2 text-sm text-gray-500 mb-6">
      <router-link to="/" class="hover:text-indigo-600">Каталог</router-link>
      <span>→</span>
      <span v-if="product.category" class="hover:text-indigo-600 cursor-pointer"
        @click="router.push({ path: '/', query: { category: product.category } })">{{ product.category }}</span>
      <span v-if="product.category">→</span>
      <span class="text-gray-900 font-medium">{{ product.name }}</span>
    </nav>

    <div class="card p-6 lg:p-8">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Image -->
        <div class="aspect-square bg-gray-100 rounded-xl overflow-hidden">
          <img :src="imgSrc" :alt="product.name" class="w-full h-full object-cover" />
        </div>

        <!-- Info -->
        <div class="flex flex-col">
          <span v-if="product.category" class="text-sm text-indigo-600 font-medium mb-2">{{ product.category }}</span>
          <h1 class="text-2xl lg:text-3xl font-bold text-gray-900 mb-2">{{ product.name }}</h1>
          <p class="text-sm text-gray-400 mb-4">Артикул: {{ product.sku }}</p>

          <p class="text-gray-600 leading-relaxed mb-6 flex-1">{{ product.description }}</p>

          <div class="border-t border-gray-100 pt-6">
            <!-- Price -->
            <div class="flex items-baseline gap-2 mb-6">
              <span class="text-3xl font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
              <span class="text-gray-400">/ {{ product.unit }}</span>
            </div>

            <!-- Quantity + Add to cart -->
            <div v-if="!inCart" class="flex items-center gap-4">
              <div class="flex items-center border border-gray-300 rounded-lg">
                <button @click="quantity = Math.max(1, quantity - 1)"
                  class="w-10 h-10 flex items-center justify-center text-gray-600 hover:bg-gray-100 rounded-l-lg transition-colors">−</button>
                <input v-model.number="quantity" type="number" min="1"
                  class="w-14 h-10 text-center border-x border-gray-300 outline-none text-sm" />
                <button @click="quantity++"
                  class="w-10 h-10 flex items-center justify-center text-gray-600 hover:bg-gray-100 rounded-r-lg transition-colors">+</button>
              </div>
              <button @click="addToCart" class="btn-primary flex-1 !py-3 text-base">
                Добавить в корзину
              </button>
            </div>

            <!-- Already in cart -->
            <div v-else class="space-y-3">
              <div class="flex items-center gap-3 bg-green-50 border border-green-200 rounded-lg p-3">
                <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                <span class="text-green-700 font-medium">В корзине: {{ cartQty }} шт.</span>
              </div>
              <div class="flex gap-3">
                <button @click="cart.addItem(product!.id, 1)" class="btn-primary flex-1">Ещё +1</button>
                <router-link to="/cart" class="btn-secondary flex-1 text-center">Перейти в корзину</router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
