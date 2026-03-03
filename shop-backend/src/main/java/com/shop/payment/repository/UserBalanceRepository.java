package com.shop.payment.repository;

import com.shop.payment.entity.UserBalance;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserBalanceRepository extends CrudRepository<UserBalance, UUID> {
    @Query("SELECT * FROM user_balances WHERE user_id = :userId")
    Optional<UserBalance> findByUserId(UUID userId);
}
