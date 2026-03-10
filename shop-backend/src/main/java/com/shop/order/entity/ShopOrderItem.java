package com.shop.order.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class ShopOrderItem {
    @EqualsAndHashCode.Include
    @Id private UUID id;
    private UUID orderId;
    private UUID productId;
    private String productName;
    private String productSku;
    private String productImageUrl;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
