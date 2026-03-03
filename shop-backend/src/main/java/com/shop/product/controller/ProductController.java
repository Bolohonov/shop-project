package com.shop.product.controller;

import com.shop.common.response.ApiResponse;
import com.shop.product.dto.ProductPageResponse;
import com.shop.product.dto.ProductResponse;
import com.shop.product.dto.ProductSearchRequest;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ApiResponse<ProductPageResponse> search(ProductSearchRequest req) {
        return ApiResponse.ok(productService.search(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.ok(productService.getCategories());
    }
}
