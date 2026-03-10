package com.shop.cart.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
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
