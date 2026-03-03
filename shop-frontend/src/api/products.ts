import api from './client'
export const productsApi = {
  search: (params: any) => api.get('/products', { params }),
  getById: (id: string) => api.get(`/products/${id}`),
  getCategories: () => api.get('/products/categories'),
}
