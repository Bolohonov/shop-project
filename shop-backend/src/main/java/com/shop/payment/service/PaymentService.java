package com.shop.payment.service;

import com.shop.common.config.AppProperties;
import com.shop.common.exception.AppException;
import com.shop.payment.entity.PaymentTransaction;
import com.shop.payment.entity.UserBalance;
import com.shop.payment.repository.PaymentTransactionRepository;
import com.shop.payment.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class PaymentService {
    private final UserBalanceRepository balanceRepo;
    private final PaymentTransactionRepository txRepo;
    private final AppProperties props;

    public void createBalance(UUID userId) {
        if (balanceRepo.findByUserId(userId).isEmpty()) {
            balanceRepo.save(UserBalance.builder()
                .userId(userId).balance(props.getPayment().getDefaultBalance())
                .updatedAt(Instant.now()).build());
        }
    }

    public BigDecimal getBalance(UUID userId) {
        return balanceRepo.findByUserId(userId)
            .map(UserBalance::getBalance)
            .orElse(BigDecimal.ZERO);
    }

    public boolean hasSufficientBalance(UUID userId, BigDecimal amount) {
        return getBalance(userId).compareTo(amount) >= 0;
    }

    @Transactional
    public void charge(UUID userId, UUID orderId, BigDecimal amount) {
        UserBalance balance = balanceRepo.findByUserId(userId)
            .orElseThrow(() -> AppException.badRequest("Баланс не найден"));
        if (balance.getBalance().compareTo(amount) < 0)
            throw AppException.badRequest("Недостаточно средств на балансе");
        balance.setBalance(balance.getBalance().subtract(amount));
        balance.setUpdatedAt(Instant.now());
        balanceRepo.save(balance);
        txRepo.save(PaymentTransaction.builder()
            .userId(userId).orderId(orderId).amount(amount)
            .type("CHARGE").status("SUCCESS").createdAt(Instant.now()).build());
        log.info("Payment charged: user={} order={} amount={}", userId, orderId, amount);
    }

    @Transactional
    public void topUp(UUID userId, BigDecimal amount) {
        UserBalance balance = balanceRepo.findByUserId(userId)
            .orElseThrow(() -> AppException.badRequest("Баланс не найден"));
        balance.setBalance(balance.getBalance().add(amount));
        balance.setUpdatedAt(Instant.now());
        balanceRepo.save(balance);
        txRepo.save(PaymentTransaction.builder()
            .userId(userId).amount(amount).type("TOPUP").status("SUCCESS")
            .createdAt(Instant.now()).build());
    }
}
