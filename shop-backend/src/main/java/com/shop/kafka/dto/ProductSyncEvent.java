package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSyncEvent {
    private UUID productId;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String unit;
    private String imageUrl;
    private boolean active;
    private String action; // UPSERT, DELETE
}
