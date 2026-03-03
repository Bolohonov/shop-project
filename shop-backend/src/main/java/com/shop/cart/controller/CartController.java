package com.shop.cart.controller;

import com.shop.auth.entity.User;
import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.dto.UpdateCartItemRequest;
import com.shop.cart.service.CartService;
import com.shop.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/cart") @RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@AuthenticationPrincipal User user, @RequestBody AddToCartRequest req) {
        return ApiResponse.ok(cartService.addItem(user.getId(), req));
    }

    @PutMapping("/items/{productId}")
    public ApiResponse<CartResponse> updateItem(@AuthenticationPrincipal User user,
            @PathVariable UUID productId, @RequestBody UpdateCartItemRequest req) {
        return ApiResponse.ok(cartService.updateItem(user.getId(), productId, req.getQuantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<CartResponse> removeItem(@AuthenticationPrincipal User user, @PathVariable UUID productId) {
        return ApiResponse.ok(cartService.removeItem(user.getId(), productId));
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId()); return ApiResponse.ok(null, "Корзина очищена");
    }
}
