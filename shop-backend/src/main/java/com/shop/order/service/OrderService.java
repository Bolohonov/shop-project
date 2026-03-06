package com.shop.order.service;

import com.shop.auth.entity.User;
import com.shop.cart.entity.CartItem;
import com.shop.cart.service.CartService;
import com.shop.common.exception.AppException;
import com.shop.common.config.AppProperties;
import com.shop.kafka.dto.ShopOrderCreatedEvent;
import com.shop.kafka.producer.ShopOrderProducer;
import com.shop.order.dto.*;
import com.shop.order.entity.*;
import com.shop.order.repository.*;
import com.shop.payment.service.PaymentService;
import com.shop.product.entity.Product;
import com.shop.product.service.ProductService;
import com.shop.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class OrderService {
    private final ShopOrderRepository orderRepo;
    private final ShopOrderItemRepository itemRepo;
    private final OrderStatusHistoryRepository historyRepo;
    private final CartService cartService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final ShopOrderProducer kafkaProducer;
    private final SseService sseService;
    private final AppProperties props;

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "NEW", "Новый",
            "PICKING", "Комплектуется",
            "SHIPPED", "Отправлен",
            "DELIVERED", "Получен",
            "ARCHIVED", "Архив",
            "CANCELLED", "Отменён"
    );

    @Transactional
    public OrderResponse checkout(User user, String comment) {
        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) throw AppException.badRequest("Корзина пуста");

        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        List<ShopOrderItem> orderItems = new ArrayList<>();
        List<ShopOrderCreatedEvent.ItemInfo> kafkaItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Product product = productService.findEntityById(ci.getProductId());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);
            orderItems.add(ShopOrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productImageUrl(product.getImageUrl())
                    .quantity(ci.getQuantity())
                    .price(product.getPrice())
                    .totalPrice(lineTotal)
                    .build());
            kafkaItems.add(ShopOrderCreatedEvent.ItemInfo.builder()
                    .sku(product.getSku())
                    .name(product.getName())
                    .quantity(BigDecimal.valueOf(ci.getQuantity()))
                    .price(product.getPrice())
                    .build());
        }

        // Check balance
        if (!paymentService.hasSufficientBalance(user.getId(), total))
            throw AppException.badRequest("Недостаточно средств на балансе. Текущий баланс: "
                    + paymentService.getBalance(user.getId()) + " ₽, необходимо: " + total + " ₽");

        // Create order
        long seq = orderRepo.nextOrderNumber();
        String orderNumber = String.format("SHOP-%05d", seq);
        UUID shopUuid = UUID.randomUUID();

        ShopOrder order = ShopOrder.builder()
                .userId(user.getId())
                .orderNumber(orderNumber)
                .status("NEW")
                .totalAmount(total)
                .comment(comment)
                .shopOrderUuid(shopUuid)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        order = orderRepo.save(order);

        // Save items
        for (ShopOrderItem item : orderItems) {
            item.setOrderId(order.getId());
            itemRepo.save(item);
        }

        // Status history
        historyRepo.save(OrderStatusHistory.builder()
                .orderId(order.getId()).newStatus("NEW")
                .changedBy("system").comment("Заказ оформлен")
                .changedAt(Instant.now()).build());

        // Charge payment
        paymentService.charge(user.getId(), order.getId(), total);

        // Clear cart
        cartService.clearCart(user.getId());

        // Send to CRM via Kafka (outbox)
        kafkaProducer.enqueueOrderCreated(ShopOrderCreatedEvent.builder()
                .shopOrderId(orderNumber)
                .shopOrderUuid(shopUuid)
                .customer(ShopOrderCreatedEvent.CustomerInfo.builder()
                        .externalId(user.getId().toString())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .address(user.getAddress())
                        .build())
                .items(kafkaItems)
                .totalAmount(total)
                .comment(comment)
                .createdAt(Instant.now())
                .tenantSchema(props.getKafka().getCrmTenantSchema())
                .build());

        log.info("Order created: {} total={} items={}", orderNumber, total, orderItems.size());
        return toResponse(order, true);
    }

    public OrderPageResponse listOrders(UUID userId, int page, int size) {
        size = Math.min(size, 100);
        int offset = page * size;
        var orders = orderRepo.findByUserId(userId, size, offset);
        long total = orderRepo.countByUserId(userId);
        return OrderPageResponse.builder()
                .content(orders.stream().map(o -> toResponse(o, false)).toList())
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .page(page).size(size)
                .build();
    }

    public OrderResponse getOrder(UUID userId, UUID orderId) {
        ShopOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Заказ"));
        if (!order.getUserId().equals(userId))
            throw AppException.forbidden("Нет доступа к этому заказу");
        return toResponse(order, true);
    }

    private OrderResponse toResponse(ShopOrder order, boolean withDetails) {
        var builder = OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusLabel(STATUS_LABELS.getOrDefault(order.getStatus(), order.getStatus()))
                .totalAmount(order.getTotalAmount())
                .comment(order.getComment())
                .shopOrderUuid(order.getShopOrderUuid())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());

        if (withDetails) {
            builder.items(itemRepo.findByOrderId(order.getId()).stream()
                    .map(item -> OrderItemResponse.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .productSku(item.getProductSku())
                            .productImageUrl(item.getProductImageUrl())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .toList());
            builder.statusHistory(historyRepo.findByOrderId(order.getId()).stream()
                    .map(h -> StatusHistoryResponse.builder()
                            .previousStatus(h.getPreviousStatus())
                            .newStatus(h.getNewStatus())
                            .newStatusLabel(STATUS_LABELS.getOrDefault(h.getNewStatus(), h.getNewStatus()))
                            .changedBy(h.getChangedBy())
                            .comment(h.getComment())
                            .changedAt(h.getChangedAt())
                            .build())
                    .toList());
        }
        return builder.build();
    }
}
