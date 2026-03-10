package com.shop.auth.dto;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
