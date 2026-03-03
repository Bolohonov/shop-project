package com.shop.auth.controller;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RefreshRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserInfo;
import com.shop.auth.entity.User;
import com.shop.auth.service.AuthService;
import com.shop.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal User user) {
        authService.logout(user.getId());
        return ApiResponse.ok(null, "Вы вышли из системы");
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(authService.getMe(user.getId()));
    }
}
