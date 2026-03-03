<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productsApi } from '@/api/products'
import ProductCard from '@/components/product/ProductCard.vue'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()

const products = ref<Product[]>([])
const categories = ref<string[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)

// Filters
const query = ref((route.query.q as string) || '')
const category = ref((route.query.category as string) || '')
const sortBy = ref((route.query.sort as string) || 'name_asc')
const page = ref(Number(route.query.page) || 0)
const size = ref(Number(route.query.size) || 10)
const viewMode = ref<'grid' | 'list'>('grid')

const sizes = [2, 5, 10, 20, 50, 100]
const sortOptions = [
  { value: 'name_asc', label: 'По названию А→Я' },
  { value: 'name_desc', label: 'По названию Я→А' },
  { value: 'price_asc', label: 'По цене ↑' },
  { value: 'price_desc', label: 'По цене ↓' },
]

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await productsApi.search({
      query: query.value || undefined,
      category: category.value || undefined,
      sortBy: sortBy.value,
      page: page.value,
      size: size.value,
    })
    products.value = data.data.content
    totalElements.value = data.data.totalElements
    totalPages.value = data.data.totalPages
    if (data.data.categories) categories.value = data.data.categories
  } finally { loading.value = false }
}

function updateRoute() {
  const q: any = {}
  if (query.value) q.q = query.value
  if (category.value) q.category = category.value
  if (sortBy.value !== 'name_asc') q.sort = sortBy.value
  if (page.value > 0) q.page = page.value
  if (size.value !== 10) q.size = size.value
  router.replace({ query: q })
}

function applySearch() { page.value = 0; updateRoute(); fetchProducts() }
function setCategory(cat: string) { category.value = cat === category.value ? '' : cat; applySearch() }
function setPage(p: number) { page.value = p; updateRoute(); fetchProducts(); window.scrollTo(0, 0) }
function setSize(s: number) { size.value = s; page.value = 0; updateRoute(); fetchProducts() }

watch(sortBy, () => applySearch())
onMounted(fetchProducts)
</script>

<template>
  <div>
    <!-- Search & Filters -->
    <div class="mb-6 space-y-4">
      <!-- Search bar -->
      <div class="flex gap-3">
        <div class="relative flex-1">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
          <input v-model="query" @keyup.enter="applySearch" type="text"
            placeholder="Поиск по названию, описанию, артикулу..."
            class="input-field pl-10" />
        </div>
        <button @click="applySearch" class="btn-primary">Найти</button>
      </div>

      <!-- Categories -->
      <div class="flex flex-wrap gap-2" v-if="categories.length">
        <button @click="setCategory('')"
          :class="[!category ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50']"
          class="px-3 py-1.5 rounded-full text-sm font-medium transition-colors">
          Все
        </button>
        <button v-for="cat in categories" :key="cat" @click="setCategory(cat)"
          :class="[category === cat ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50']"
          class="px-3 py-1.5 rounded-full text-sm font-medium transition-colors">
          {{ cat }}
        </button>
      </div>

      <!-- Sort & Size & View mode -->
      <div class="flex items-center justify-between flex-wrap gap-3">
        <div class="flex items-center gap-3">
          <select v-model="sortBy" class="input-field !w-auto text-sm">
            <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <div class="flex items-center gap-1 text-sm text-gray-500">
            <span>Показывать:</span>
            <button v-for="s in sizes" :key="s" @click="setSize(s)"
              :class="[size === s ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100']"
              class="w-8 h-8 rounded flex items-center justify-center text-xs font-medium transition-colors border border-gray-200">
              {{ s }}
            </button>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-500">Найдено: {{ totalElements }}</span>
          <div class="flex border border-gray-200 rounded-lg overflow-hidden">
            <button @click="viewMode = 'grid'" :class="[viewMode === 'grid' ? 'bg-indigo-600 text-white' : 'bg-white text-gray-500']"
              class="p-2 transition-colors">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 16 16"><rect x="1" y="1" width="6" height="6" rx="1"/><rect x="9" y="1" width="6" height="6" rx="1"/><rect x="1" y="9" width="6" height="6" rx="1"/><rect x="9" y="9" width="6" height="6" rx="1"/></svg>
            </button>
            <button @click="viewMode = 'list'" :class="[viewMode === 'list' ? 'bg-indigo-600 text-white' : 'bg-white text-gray-500']"
              class="p-2 transition-colors">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 16 16"><rect x="1" y="1" width="14" height="3" rx="1"/><rect x="1" y="6" width="14" height="3" rx="1"/><rect x="1" y="11" width="14" height="3" rx="1"/></svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
    </div>

    <!-- Empty state -->
    <div v-else-if="products.length === 0" class="text-center py-20">
      <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
      </svg>
      <h3 class="text-lg font-medium text-gray-600">Товары не найдены</h3>
      <p class="text-sm text-gray-400 mt-1">Попробуйте изменить параметры поиска</p>
    </div>

    <!-- Products Grid -->
    <div v-else>
      <div :class="viewMode === 'grid' ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4' : 'space-y-3'">
        <ProductCard v-for="product in products" :key="product.id" :product="product" />
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
        <button @click="setPage(page - 1)" :disabled="page === 0"
          class="btn-secondary text-sm !px-3 disabled:opacity-30">← Назад</button>
        <template v-for="p in totalPages" :key="p">
          <button v-if="p - 1 === page || Math.abs(p - 1 - page) <= 2 || p === 1 || p === totalPages"
            @click="setPage(p - 1)"
            :class="[p - 1 === page ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100']"
            class="w-9 h-9 rounded-lg text-sm font-medium border border-gray-200 transition-colors">
            {{ p }}
          </button>
          <span v-else-if="(p === 2 && page > 3) || (p === totalPages - 1 && page < totalPages - 4)"
            class="text-gray-400">…</span>
        </template>
        <button @click="setPage(page + 1)" :disabled="page >= totalPages - 1"
          class="btn-secondary text-sm !px-3 disabled:opacity-30">Вперёд →</button>
      </div>
    </div>
  </div>
</template>
