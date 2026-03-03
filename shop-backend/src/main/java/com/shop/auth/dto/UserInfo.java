package com.shop.auth.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class UserInfo {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private BigDecimal balance;
    private Instant createdAt;
}
