import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/cart'
import type { Cart } from '@/types'

export const useCartStore = defineStore('cart', () => {
  const cart = ref<Cart>({ items: [], totalAmount: 0, totalItems: 0 })

  const itemCount = computed(() => cart.value.items.reduce((sum, i) => sum + i.quantity, 0))
  const totalAmount = computed(() => cart.value.totalAmount)

  async function fetchCart() {
    try { const { data } = await cartApi.get(); cart.value = data.data }
    catch { cart.value = { items: [], totalAmount: 0, totalItems: 0 } }
  }

  async function addItem(productId: string, quantity = 1) {
    const { data } = await cartApi.addItem(productId, quantity)
    cart.value = data.data
  }

  async function updateItem(productId: string, quantity: number) {
    const { data } = await cartApi.updateItem(productId, quantity)
    cart.value = data.data
  }

  async function removeItem(productId: string) {
    const { data } = await cartApi.removeItem(productId)
    cart.value = data.data
  }

  async function clearCart() {
    await cartApi.clear()
    cart.value = { items: [], totalAmount: 0, totalItems: 0 }
  }

  function getQuantity(productId: string): number {
    return cart.value.items.find(i => i.productId === productId)?.quantity || 0
  }

  function isInCart(productId: string): boolean {
    return cart.value.items.some(i => i.productId === productId)
  }

  return { cart, itemCount, totalAmount, fetchCart, addItem, updateItem, removeItem, clearCart, getQuantity, isInCart }
})
