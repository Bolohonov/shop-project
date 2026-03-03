package com.shop.cart.repository;

import com.shop.cart.entity.CartItem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends CrudRepository<CartItem, UUID> {
    @Query("SELECT * FROM cart_items WHERE user_id = :userId ORDER BY added_at DESC")
    List<CartItem> findByUserId(UUID userId);

    @Query("SELECT * FROM cart_items WHERE user_id = :userId AND product_id = :productId")
    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    @Modifying @Query("DELETE FROM cart_items WHERE user_id = :userId")
    void deleteByUserId(UUID userId);

    @Modifying @Query("DELETE FROM cart_items WHERE user_id = :userId AND product_id = :productId")
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}
