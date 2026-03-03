<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { useSse } from '@/composables/useSse'
import { useToast } from '@/composables/useToast'
import type { Order } from '@/types'

const router = useRouter()
const toast = useToast()
const { connect } = useSse()

const orders = ref<Order[]>([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const statusColors: Record<string, string> = {
  NEW: 'bg-blue-100 text-blue-700',
  PICKING: 'bg-yellow-100 text-yellow-700',
  SHIPPED: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  ARCHIVED: 'bg-gray-100 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-700',
}

function formatPrice(n: number) { return n.toLocaleString('ru-RU') + ' ₽' }
function formatDate(s: string) {
  return new Date(s).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function fetchOrders() {
  loading.value = true
  try {
    const { data } = await ordersApi.list(page.value, 10)
    orders.value = data.data.content
    totalPages.value = data.data.totalPages
    totalElements.value = data.data.totalElements
  } finally { loading.value = false }
}

function setPage(p: number) { page.value = p; fetchOrders() }

onMounted(() => {
  fetchOrders()
  connect((event) => {
    toast.show(`Заказ ${event.orderNumber}: статус изменён на "${event.newStatus}"`, 'info')
    fetchOrders()
  })
})
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <h1 class="text-2xl font-bold mb-6">Мои заказы</h1>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
    </div>

    <div v-else-if="orders.length === 0" class="card p-12 text-center">
      <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      <h2 class="text-lg font-medium text-gray-600 mb-2">Заказов пока нет</h2>
      <router-link to="/" class="btn-primary mt-4 inline-block">Перейти в каталог</router-link>
    </div>

    <div v-else class="space-y-4">
      <div v-for="order in orders" :key="order.id"
        @click="router.push(`/orders/${order.id}`)"
        class="card p-5 cursor-pointer hover:shadow-md transition-shadow flex items-center justify-between gap-4">
        <div class="flex-1">
          <div class="flex items-center gap-3 mb-1">
            <span class="font-bold text-gray-900">{{ order.orderNumber }}</span>
            <span :class="statusColors[order.status] || 'bg-gray-100 text-gray-600'"
              class="text-xs font-semibold px-2.5 py-1 rounded-full">{{ order.statusLabel }}</span>
          </div>
          <p class="text-sm text-gray-500">{{ formatDate(order.createdAt) }}</p>
        </div>
        <div class="text-right">
          <div class="font-bold text-lg">{{ formatPrice(order.totalAmount) }}</div>
        </div>
        <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
        </svg>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-6">
        <button v-for="p in totalPages" :key="p" @click="setPage(p - 1)"
          :class="[p - 1 === page ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100']"
          class="w-9 h-9 rounded-lg text-sm font-medium border border-gray-200 transition-colors">{{ p }}</button>
      </div>
    </div>
  </div>
</template>
