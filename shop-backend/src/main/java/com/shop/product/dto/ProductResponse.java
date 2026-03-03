package com.shop.product.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String unit;
    private String imageUrl;
    private String category;
    private int stockQuantity;
}
