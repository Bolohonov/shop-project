package com.shop.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Data @Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl;
    private String frontendUrl;
    private Jwt jwt = new Jwt();
    private Email email = new Email();
    private Cors cors = new Cors();
    private Kafka kafka = new Kafka();
    private Redis redis = new Redis();
    private Payment payment = new Payment();

    @Data public static class Jwt {
        private String secret;
        private long accessTokenExpiration = 900;
        private long refreshTokenExpiration = 604800;
    }
    @Data public static class Email { private String from; private String fromName; }
    @Data public static class Cors { private List<String> allowedOrigins = List.of("http://localhost:5174"); }
    @Data public static class Kafka {
        private Topics topics = new Topics();
        private long outboxPollIntervalMs = 5000;
        /** Схема тенанта CRM для этого магазина */
        private String crmTenantSchema = "tenant_shop";
        @Data public static class Topics {
            private String shopOrderCreated = "shop.orders.created";
            private String crmOrderStatusChanged = "crm.orders.status_changed";
            private String crmProductsSync = "crm.products.sync";
        }
    }
    @Data public static class Redis { private int productCacheTtlMinutes = 60; }
    @Data public static class Payment { private BigDecimal defaultBalance = new BigDecimal("1000000.00"); }
}
