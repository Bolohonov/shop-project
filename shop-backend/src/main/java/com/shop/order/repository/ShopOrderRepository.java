package com.shop.order.repository;

import com.shop.order.entity.ShopOrder;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopOrderRepository extends CrudRepository<ShopOrder, UUID> {
    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :lim OFFSET :off")
    List<ShopOrder> findByUserId(UUID userId, int lim, int off);

    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId")
    long countByUserId(UUID userId);

    @Query("SELECT * FROM orders WHERE shop_order_uuid = :uuid")
    Optional<ShopOrder> findByShopOrderUuid(UUID uuid);

    @Modifying
    @Query("UPDATE orders SET status = :status, updated_at = NOW() WHERE id = :id")
    void updateStatus(UUID id, String status);

    @Query("SELECT nextval('order_number_seq')")
    long nextOrderNumber();
}
