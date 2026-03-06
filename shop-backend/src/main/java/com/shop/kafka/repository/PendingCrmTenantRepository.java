package com.shop.kafka.repository;

import com.shop.kafka.entity.PendingCrmTenant;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface PendingCrmTenantRepository extends CrudRepository<PendingCrmTenant, UUID> {
    Optional<PendingCrmTenant> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM pending_crm_tenants WHERE email = :email")
    void deleteByEmail(String email);
}
