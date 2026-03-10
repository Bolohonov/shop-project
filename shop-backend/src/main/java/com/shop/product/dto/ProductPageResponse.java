package com.shop.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductPageResponse {
    private List<ProductResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private List<String> categories;
}
