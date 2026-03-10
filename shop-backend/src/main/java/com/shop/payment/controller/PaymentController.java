package com.shop.payment.controller;

import com.shop.auth.entity.User;
import com.shop.common.response.ApiResponse;
import com.shop.payment.service.PaymentService;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController @RequestMapping("/payments") @RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/balance")
    public ApiResponse<BigDecimal> getBalance(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(paymentService.getBalance(user.getId()));
    }

    @PostMapping("/topup")
    public ApiResponse<BigDecimal> topUp(@AuthenticationPrincipal User user, @RequestBody TopUpRequest req) {
        paymentService.topUp(user.getId(), req.getAmount());
        return ApiResponse.ok(paymentService.getBalance(user.getId()), "Баланс пополнен");
    }

    @Getter
@Setter public static class TopUpRequest { private BigDecimal amount; }
}
