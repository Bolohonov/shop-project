package com.shop.order.repository;

import com.shop.order.entity.ShopOrderItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

public interface ShopOrderItemRepository extends CrudRepository<ShopOrderItem, UUID> {
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    List<ShopOrderItem> findByOrderId(UUID orderId);
}
