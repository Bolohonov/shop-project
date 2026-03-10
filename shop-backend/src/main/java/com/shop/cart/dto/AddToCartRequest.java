package com.shop.cart.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddToCartRequest {
    private UUID productId;
    private int quantity = 1;
}
