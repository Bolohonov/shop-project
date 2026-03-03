package com.shop.product.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProductPageResponse {
    private List<ProductResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private List<String> categories;
}
