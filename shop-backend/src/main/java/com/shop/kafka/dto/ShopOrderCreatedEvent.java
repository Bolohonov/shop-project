package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopOrderCreatedEvent {
    private String shopOrderId;
    private UUID shopOrderUuid;
    private CustomerInfo customer;
    private List<ItemInfo> items;
    private BigDecimal totalAmount;
    private String comment;
    private Instant createdAt;

    @Data @Builder public static class CustomerInfo {
        private String externalId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
    }
    @Data @Builder public static class ItemInfo {
        private String sku;
        private String name;
        private BigDecimal quantity;
        private BigDecimal price;
    }
}
