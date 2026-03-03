package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserInfo;
import com.shop.auth.entity.RefreshToken;
import com.shop.auth.entity.User;
import com.shop.auth.repository.RefreshTokenRepository;
import com.shop.auth.repository.UserRepository;
import com.shop.common.config.AppProperties;
import com.shop.common.exception.AppException;
import com.shop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;
    private final PaymentService paymentService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail().toLowerCase().trim()))
            throw AppException.conflict("Пользователь с таким email уже существует");

        User user = User.builder()
            .email(req.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .phone(req.getPhone())
            .address(req.getAddress())
            .isActive(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        user = userRepo.save(user);
        paymentService.createBalance(user.getId());
        log.info("User registered: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
            .orElseThrow(() -> AppException.badRequest("Неверный email или пароль"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
            throw AppException.badRequest("Неверный email или пароль");
        if (!user.isActive())
            throw AppException.forbidden("Аккаунт деактивирован");
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken rt = refreshRepo.findByToken(refreshTokenStr)
            .orElseThrow(() -> AppException.badRequest("Невалидный refresh token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshRepo.delete(rt);
            throw AppException.badRequest("Refresh token истёк");
        }
        User user = userRepo.findById(rt.getUserId())
            .orElseThrow(() -> AppException.notFound("Пользователь"));
        refreshRepo.delete(rt);
        return buildAuthResponse(user);
    }

    public void logout(UUID userId) { refreshRepo.deleteByUserId(userId); }

    public UserInfo getMe(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> AppException.notFound("Пользователь"));
        return toUserInfo(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        AuthResponse resp = new AuthResponse();
        resp.setAccessToken(jwtService.generateAccessToken(user.getId(), user.getEmail()));
        String rt = jwtService.generateRefreshToken();
        RefreshToken entity = RefreshToken.builder()
            .userId(user.getId())
            .token(rt)
            .expiresAt(Instant.now().plusSeconds(props.getJwt().getRefreshTokenExpiration()))
            .createdAt(Instant.now())
            .build();
        refreshRepo.save(entity);
        resp.setRefreshToken(rt);
        resp.setUser(toUserInfo(user));
        return resp;
    }

    private UserInfo toUserInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setEmail(user.getEmail());
        info.setFirstName(user.getFirstName());
        info.setLastName(user.getLastName());
        info.setPhone(user.getPhone());
        info.setAddress(user.getAddress());
        info.setCreatedAt(user.getCreatedAt());
        try { info.setBalance(paymentService.getBalance(user.getId())); } catch (Exception e) {}
        return info;
    }
}
