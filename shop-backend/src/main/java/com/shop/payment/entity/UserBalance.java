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
@Table("user_balances")
public class UserBalance {
    @Id private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private Instant updatedAt;
}
