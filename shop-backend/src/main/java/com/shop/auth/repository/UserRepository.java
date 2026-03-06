package com.shop.auth.repository;

import com.shop.auth.entity.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    @Query("SELECT * FROM users WHERE email = :email")
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jdbc.repository.query.Modifying
    @Query("UPDATE users SET crm_tenant_schema = :schema WHERE email = :email")
    void updateCrmTenantSchema(String email, String schema);
}
