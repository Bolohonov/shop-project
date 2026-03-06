package com.shop.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    /** Схема тенанта CRM — заполняется при получении события crm.tenant.created */
    private String crmTenantSchema;
}
