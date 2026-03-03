package com.shop.kafka.consumer;

import com.shop.kafka.dto.OrderStatusChangedEvent;
import com.shop.order.entity.OrderStatusHistory;
import com.shop.order.entity.ShopOrder;
import com.shop.order.repository.OrderStatusHistoryRepository;
import com.shop.order.repository.ShopOrderRepository;
import com.shop.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Slf4j @Component @RequiredArgsConstructor
public class OrderStatusConsumer {
    private final ShopOrderRepository orderRepo;
    private final OrderStatusHistoryRepository historyRepo;
    private final SseService sseService;

    @KafkaListener(
        topics = "${app.kafka.topics.crm-order-status-changed}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "statusListenerFactory"
    )
    @Transactional
    public void onStatusChanged(ConsumerRecord<String, OrderStatusChangedEvent> record, Acknowledgment ack) {
        OrderStatusChangedEvent event = record.value();
        log.info("Kafka received status change: shopOrderUuid={} {} → {}",
            event.getShopOrderUuid(), event.getPreviousStatus(), event.getNewStatus());
        try {
            if (event.getShopOrderUuid() == null) { ack.acknowledge(); return; }
            var orderOpt = orderRepo.findByShopOrderUuid(event.getShopOrderUuid());
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for shopOrderUuid={}", event.getShopOrderUuid());
                ack.acknowledge(); return;
            }
            ShopOrder order = orderOpt.get();
            String oldStatus = order.getStatus();
            order.setStatus(event.getNewStatus());
            if (event.getCrmOrderId() != null) order.setCrmOrderId(event.getCrmOrderId());
            orderRepo.save(order);

            historyRepo.save(OrderStatusHistory.builder()
                .orderId(order.getId())
                .previousStatus(oldStatus)
                .newStatus(event.getNewStatus())
                .changedBy(event.getChangedBy())
                .comment(event.getComment())
                .changedAt(event.getChangedAt() != null ? event.getChangedAt() : Instant.now())
                .build());

            // SSE push to user
            sseService.notifyUser(order.getUserId(), "order.status_changed", new java.util.HashMap<>() {{
                put("orderId", order.getId());
                put("orderNumber", order.getOrderNumber());
                put("previousStatus", event.getPreviousStatus());
                put("newStatus", event.getNewStatus());
                put("comment", event.getComment());
            }});

            log.info("Order {} status updated: {} → {}", order.getOrderNumber(), oldStatus, event.getNewStatus());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process status change: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
