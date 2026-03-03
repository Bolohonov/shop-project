import api from './client'
export const ordersApi = {
  checkout: (comment?: string) => api.post('/orders/checkout', { comment }),
  list: (page = 0, size = 10) => api.get('/orders', { params: { page, size } }),
  getById: (id: string) => api.get(`/orders/${id}`),
}
