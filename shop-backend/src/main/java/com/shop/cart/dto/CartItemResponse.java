package com.shop.cart.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private BigDecimal price;
    private String unit;
    private int quantity;
    private BigDecimal totalPrice;
}
