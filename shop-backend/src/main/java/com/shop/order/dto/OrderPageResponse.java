package com.shop.order.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderPageResponse {
    private List<OrderResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
