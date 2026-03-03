package com.shop.order.controller;

import com.shop.auth.entity.User;
import com.shop.common.response.ApiResponse;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.dto.OrderPageResponse;
import com.shop.order.dto.OrderResponse;
import com.shop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/orders") @RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(
            @AuthenticationPrincipal User user, @RequestBody(required = false) CreateOrderRequest req) {
        String comment = req != null ? req.getComment() : null;
        return ApiResponse.ok(orderService.checkout(user, comment));
    }

    @GetMapping
    public ApiResponse<OrderPageResponse> listOrders(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(orderService.listOrders(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ApiResponse.ok(orderService.getOrder(user.getId(), id));
    }
}
