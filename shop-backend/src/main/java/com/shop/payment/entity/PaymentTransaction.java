package com.shop.payment.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_transactions")
public class PaymentTransaction {
    @Id private UUID id;
    private UUID userId;
    private UUID orderId;
    private BigDecimal amount;
    private String type;   // CHARGE, REFUND, TOPUP
    private String status; // SUCCESS, FAILED
    private Instant createdAt;
}
