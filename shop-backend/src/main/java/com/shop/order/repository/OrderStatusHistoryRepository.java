package com.shop.order.repository;

import com.shop.order.entity.OrderStatusHistory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends CrudRepository<OrderStatusHistory, UUID> {
    @Query("SELECT * FROM order_status_history WHERE order_id = :orderId ORDER BY changed_at ASC")
    List<OrderStatusHistory> findByOrderId(UUID orderId);
}
