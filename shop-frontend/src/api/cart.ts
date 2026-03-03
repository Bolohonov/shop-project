import api from './client'
export const cartApi = {
  get: () => api.get('/cart'),
  addItem: (productId: string, quantity: number) => api.post('/cart/items', { productId, quantity }),
  updateItem: (productId: string, quantity: number) => api.put(`/cart/items/${productId}`, { quantity }),
  removeItem: (productId: string) => api.delete(`/cart/items/${productId}`),
  clear: () => api.delete('/cart'),
}
