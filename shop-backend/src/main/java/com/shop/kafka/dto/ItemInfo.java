package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemInfo {
    private String sku;
    private String name;
    private BigDecimal quantity;
    private BigDecimal price;
}
