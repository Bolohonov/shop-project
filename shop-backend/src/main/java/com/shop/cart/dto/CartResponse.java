package com.shop.cart.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int totalItems;
}
