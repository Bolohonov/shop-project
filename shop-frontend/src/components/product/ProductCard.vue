<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useToast } from '@/composables/useToast'
import type { Product } from '@/types'

const props = defineProps<{ product: Product }>()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const toast = useToast()

const inCart = computed(() => cart.isInCart(props.product.id))
const quantity = computed(() => cart.getQuantity(props.product.id))

const imgSrc = computed(() => props.product.imageUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(props.product.name)}&background=6366f1&color=fff&size=400`)

async function addToCart() {
  if (!auth.isAuthenticated) { router.push('/login'); return }
  try {
    await cart.addItem(props.product.id)
    toast.success(`${props.product.name} добавлен в корзину`)
  } catch (e: any) { toast.error(e.response?.data?.message || 'Ошибка') }
}

function formatPrice(price: number) {
  return price.toLocaleString('ru-RU', { minimumFractionDigits: 0 }) + ' ₽'
}
</script>

<template>
  <div class="card group hover:shadow-md transition-all duration-200 flex flex-col cursor-pointer"
       @click="router.push(`/product/${product.id}`)">
    <!-- Image -->
    <div class="aspect-[4/3] bg-gray-100 relative overflow-hidden">
      <img :src="imgSrc" :alt="product.name"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        loading="lazy" @error="($event.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(product.name)}&background=6366f1&color=fff&size=400`" />
      <span v-if="product.category"
        class="absolute top-2 left-2 bg-white/90 backdrop-blur-sm text-xs font-medium text-gray-700 px-2 py-1 rounded-full">
        {{ product.category }}
      </span>
    </div>

    <!-- Content -->
    <div class="p-4 flex flex-col flex-1">
      <h3 class="font-semibold text-gray-900 line-clamp-2 mb-1 group-hover:text-indigo-600 transition-colors">
        {{ product.name }}
      </h3>
      <p class="text-sm text-gray-500 line-clamp-2 mb-3 flex-1">{{ product.description }}</p>

      <div class="flex items-end justify-between mt-auto">
        <div>
          <div class="text-lg font-bold text-gray-900">{{ formatPrice(product.price) }}</div>
          <div class="text-xs text-gray-400">/ {{ product.unit }}</div>
        </div>

        <!-- Add to cart -->
        <div @click.stop>
          <button v-if="!inCart" @click="addToCart" class="btn-primary text-sm !px-3 !py-1.5">
            В корзину
          </button>
          <div v-else class="flex items-center gap-2">
            <button @click="cart.updateItem(product.id, quantity - 1)"
              class="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:bg-gray-100">−</button>
            <span class="text-sm font-semibold w-5 text-center">{{ quantity }}</span>
            <button @click="cart.updateItem(product.id, quantity + 1)"
              class="w-7 h-7 rounded-full bg-indigo-600 text-white flex items-center justify-center hover:bg-indigo-700">+</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
