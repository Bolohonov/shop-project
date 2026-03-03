package com.shop.auth.repository;

import com.shop.auth.entity.RefreshToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
    @Query("SELECT * FROM refresh_tokens WHERE token = :token")
    Optional<RefreshToken> findByToken(String token);
    @Modifying @Query("DELETE FROM refresh_tokens WHERE user_id = :userId")
    void deleteByUserId(UUID userId);
}
