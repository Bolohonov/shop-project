export interface Product {
  id: string; name: string; description: string; sku: string;
  price: number; unit: string; imageUrl: string; category: string; stockQuantity: number;
}

export interface CartItem {
  productId: string; productName: string; productSku: string;
  productImageUrl: string; price: number; unit: string; quantity: number; totalPrice: number;
}

export interface Cart { items: CartItem[]; totalAmount: number; totalItems: number; }

export interface Order {
  id: string; orderNumber: string; status: string; statusLabel: string;
  totalAmount: number; comment: string; shopOrderUuid: string;
  items?: OrderItem[]; statusHistory?: StatusHistory[];
  createdAt: string; updatedAt: string;
}

export interface OrderItem {
  productId: string; productName: string; productSku: string;
  productImageUrl: string; quantity: number; price: number; totalPrice: number;
}

export interface StatusHistory {
  previousStatus: string; newStatus: string; newStatusLabel: string;
  changedBy: string; comment: string; changedAt: string;
}

export interface UserInfo {
  id: string; email: string; firstName: string; lastName: string;
  phone: string; address: string; balance: number; createdAt: string;
}

export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; page: number; size: number;
}

export interface ApiResponse<T> { success: boolean; data: T; message: string; }
