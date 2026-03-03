<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { useSse } from '@/composables/useSse'
import { useToast } from '@/composables/useToast'
import type { Order } from '@/types'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { connect } = useSse()

const order = ref<Order | null>(null)
const loading = ref(true)

const statusColors: Record<string, string> = {
  NEW: 'bg-blue-100 text-blue-700 border-blue-300',
  PICKING: 'bg-yellow-100 text-yellow-700 border-yellow-300',
  SHIPPED: 'bg-purple-100 text-purple-700 border-purple-300',
  DELIVERED: 'bg-green-100 text-green-700 border-green-300',
  ARCHIVED: 'bg-gray-100 text-gray-600 border-gray-300',
  CANCELLED: 'bg-red-100 text-red-700 border-red-300',
}

const statusSteps = ['NEW', 'PICKING', 'SHIPPED', 'DELIVERED']

const currentStep = computed(() => {
  if (!order.value) return 0
  const idx = statusSteps.indexOf(order.value.status)
  return idx >= 0 ? idx : 0
})

function formatPrice(n: number) { return n.toLocaleString('ru-RU') + ' ₽' }
function formatDate(s: string) {
  return new Date(s).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function imgSrc(url: string, name: string) {
  return url || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=6366f1&color=fff&size=200`
}

async function fetchOrder() {
  try {
    const { data } = await ordersApi.getById(route.params.id as string)
    order.value = data.data
  } catch { router.push('/orders') }
  finally { loading.value = false }
}

onMounted(() => {
  fetchOrder()
  connect((event) => {
    if (event.orderId === route.params.id) {
      toast.show(`Статус обновлён: ${event.newStatus}`, 'info')
      fetchOrder()
    }
  })
})
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
    </div>

    <div v-else-if="order">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <router-link to="/orders" class="hover:text-indigo-600">Мои заказы</router-link>
        <span>→</span>
        <span class="text-gray-900 font-medium">{{ order.orderNumber }}</span>
      </nav>

      <!-- Header -->
      <div class="card p-6 mb-6">
        <div class="flex items-center justify-between flex-wrap gap-4 mb-6">
          <div>
            <h1 class="text-2xl font-bold">{{ order.orderNumber }}</h1>
            <p class="text-sm text-gray-500 mt-1">{{ formatDate(order.createdAt) }}</p>
          </div>
          <div class="text-right">
            <span :class="statusColors[order.status]"
              class="text-sm font-semibold px-3 py-1.5 rounded-full border">{{ order.statusLabel }}</span>
            <div class="text-2xl font-bold mt-2">{{ formatPrice(order.totalAmount) }}</div>
          </div>
        </div>

        <!-- Progress Steps -->
        <div class="flex items-center justify-between mb-2">
          <div v-for="(step, idx) in statusSteps" :key="step" class="flex-1 flex items-center">
            <div class="flex flex-col items-center flex-1">
              <div :class="[
                idx <= currentStep ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-500'
              ]" class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors">
                <svg v-if="idx < currentStep" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                <span v-else>{{ idx + 1 }}</span>
              </div>
              <span class="text-xs mt-1.5 text-gray-500 text-center">
                {{ { NEW: 'Новый', PICKING: 'Сборка', SHIPPED: 'Доставка', DELIVERED: 'Получен' }[step] }}
              </span>
            </div>
            <div v-if="idx < statusSteps.length - 1"
              :class="[idx < currentStep ? 'bg-indigo-600' : 'bg-gray-200']"
              class="h-0.5 flex-1 -mt-5 transition-colors"></div>
          </div>
        </div>
      </div>

      <!-- Items -->
      <div class="card mb-6">
        <h2 class="font-bold text-lg px-6 pt-5 pb-3">Товары</h2>
        <div class="divide-y divide-gray-100">
          <div v-for="item in order.items" :key="item.productSku" class="flex items-center gap-4 px-6 py-4">
            <img :src="imgSrc(item.productImageUrl, item.productName)" :alt="item.productName"
              class="w-14 h-14 rounded-lg object-cover bg-gray-100 flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <h4 class="font-medium text-gray-900 truncate">{{ item.productName }}</h4>
              <p class="text-sm text-gray-400">{{ item.productSku }}</p>
            </div>
            <div class="text-sm text-gray-500">{{ item.quantity }} × {{ formatPrice(item.price) }}</div>
            <div class="font-semibold w-28 text-right">{{ formatPrice(item.totalPrice) }}</div>
          </div>
        </div>
      </div>

      <!-- Status History (timeline) -->
      <div v-if="order.statusHistory?.length" class="card p-6">
        <h2 class="font-bold text-lg mb-4">История статусов</h2>
        <div class="space-y-0">
          <div v-for="(h, idx) in order.statusHistory" :key="idx" class="flex gap-4">
            <div class="flex flex-col items-center">
              <div class="w-3 h-3 rounded-full bg-indigo-600 ring-4 ring-indigo-100"></div>
              <div v-if="idx < order.statusHistory!.length - 1" class="w-0.5 flex-1 bg-indigo-200 my-1"></div>
            </div>
            <div class="pb-6 flex-1">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-900">{{ h.newStatusLabel }}</span>
                <span v-if="h.changedBy" class="text-xs text-gray-400">{{ h.changedBy }}</span>
              </div>
              <p class="text-xs text-gray-400 mt-0.5">{{ formatDate(h.changedAt) }}</p>
              <p v-if="h.comment" class="text-sm text-gray-600 mt-1 bg-gray-50 rounded-lg px-3 py-2">{{ h.comment }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Comment -->
      <div v-if="order.comment" class="card p-6 mt-6">
        <h2 class="font-bold text-lg mb-2">Комментарий</h2>
        <p class="text-gray-600">{{ order.comment }}</p>
      </div>
    </div>
  </div>
</template>
