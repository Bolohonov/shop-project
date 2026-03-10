package com.shop.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerInfo {
    private String externalId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
}
