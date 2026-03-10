package com.shop.order.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderPageResponse {
    private List<OrderResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
