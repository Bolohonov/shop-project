package com.shop.cart.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_items")
public class CartItem {
    @Id private UUID id;
    private UUID userId;
    private UUID productId;
    private int quantity;
    private Instant addedAt;
}
