import api from './client'
export const paymentsApi = {
  getBalance: () => api.get('/payments/balance'),
  topUp: (amount: number) => api.post('/payments/topup', { amount }),
}
