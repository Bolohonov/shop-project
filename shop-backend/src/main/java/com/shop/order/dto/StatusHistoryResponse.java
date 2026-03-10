package com.shop.order.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.time.Instant;

@Getter
@Setter
@Builder
public class StatusHistoryResponse {
    private String previousStatus;
    private String newStatus;
    private String newStatusLabel;
    private String changedBy;
    private String comment;
    private Instant changedAt;
}
