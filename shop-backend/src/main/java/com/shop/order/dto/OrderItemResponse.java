package com.shop.order.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
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
