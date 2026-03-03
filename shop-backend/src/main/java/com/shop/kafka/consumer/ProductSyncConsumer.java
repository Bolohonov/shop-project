package com.shop.kafka.consumer;

import com.shop.kafka.dto.ProductSyncEvent;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j @Component @RequiredArgsConstructor
public class ProductSyncConsumer {
    private final ProductService productService;

    @KafkaListener(
        topics = "${app.kafka.topics.crm-products-sync}",
        groupId = "shop-product-sync",
        containerFactory = "productSyncListenerFactory"
    )
    public void onProductSync(ConsumerRecord<String, ProductSyncEvent> record, Acknowledgment ack) {
        ProductSyncEvent event = record.value();
        log.info("Product sync received: sku={} action={}", event.getSku(), event.getAction());
        try {
            productService.syncFromCrm(event.getProductId(), event.getName(), event.getDescription(),
                event.getSku(), event.getPrice(), event.getUnit(), event.getImageUrl(), event.isActive());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Product sync failed: sku={} error={}", event.getSku(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
