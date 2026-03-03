package com.shop.product.entity;

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
@Table("products")
public class Product {
    @Id private UUID id;
    private UUID crmProductId;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String unit;
    private String imageUrl;
    private String category;
    private boolean isActive;
    private int stockQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
