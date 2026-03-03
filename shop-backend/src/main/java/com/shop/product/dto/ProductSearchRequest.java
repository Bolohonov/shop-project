package com.shop.product.dto;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String query;
    private String category;
    private String sortBy = "name_asc";
    private int page = 0;
    private int size = 10;
}
