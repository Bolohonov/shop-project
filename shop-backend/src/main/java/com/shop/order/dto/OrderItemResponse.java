package com.shop.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
