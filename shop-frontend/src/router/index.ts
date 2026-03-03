import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory('/shop/'),
  routes: [
    { path: '/', name: 'catalog', component: () => import('@/views/catalog/CatalogView.vue') },
    { path: '/product/:id', name: 'product', component: () => import('@/views/catalog/ProductView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue') },
    { path: '/cart', name: 'cart', component: () => import('@/views/cart/CartView.vue'), meta: { auth: true } },
    { path: '/orders', name: 'orders', component: () => import('@/views/orders/OrdersView.vue'), meta: { auth: true } },
    { path: '/orders/:id', name: 'order', component: () => import('@/views/orders/OrderDetailView.vue'), meta: { auth: true } },
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.user && localStorage.getItem('accessToken')) await auth.init()
  if (to.meta.auth && !auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
})

export default router
