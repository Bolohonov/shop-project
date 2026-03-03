package com.shop.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.config.AppProperties;
import com.shop.common.exception.AppException;
import com.shop.product.dto.ProductPageResponse;
import com.shop.product.dto.ProductResponse;
import com.shop.product.dto.ProductSearchRequest;
import com.shop.product.entity.Product;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties props;

    private static final String CACHE_PREFIX = "shop:product:";
    private static final String CACHE_LIST_PREFIX = "shop:products:";
    private static final String CACHE_CATEGORIES = "shop:categories";

    public ProductPageResponse search(ProductSearchRequest req) {
        int size = Math.min(req.getSize(), 100);
        int offset = req.getPage() * size;

        String cacheKey = CACHE_LIST_PREFIX + req.getQuery() + ":" + req.getCategory()
            + ":" + req.getSortBy() + ":" + req.getPage() + ":" + size;

        // Check Redis cache
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, ProductPageResponse.class);
            }
        } catch (Exception e) {
            log.debug("Redis cache miss: {}", e.getMessage());
        }

        var products = productRepo.search(
            blankToNull(req.getQuery()), blankToNull(req.getCategory()),
            req.getSortBy(), size, offset);
        long total = productRepo.countSearch(blankToNull(req.getQuery()), blankToNull(req.getCategory()));
        List<String> categories = getCategories();

        var response = ProductPageResponse.builder()
            .content(products.stream().map(this::toResponse).toList())
            .totalElements(total)
            .totalPages((int) Math.ceil((double) total / size))
            .page(req.getPage()).size(size)
            .categories(categories)
            .build();

        // Cache result
        try {
            redisTemplate.opsForValue().set(cacheKey, response,
                Duration.ofMinutes(props.getRedis().getProductCacheTtlMinutes()));
        } catch (Exception e) { log.debug("Redis set error: {}", e.getMessage()); }

        return response;
    }

    public ProductResponse getById(UUID id) {
        String cacheKey = CACHE_PREFIX + id;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) return objectMapper.convertValue(cached, ProductResponse.class);
        } catch (Exception e) { log.debug("Redis cache miss"); }

        Product product = productRepo.findById(id)
            .orElseThrow(() -> AppException.notFound("Товар"));
        var resp = toResponse(product);

        try {
            redisTemplate.opsForValue().set(cacheKey, resp,
                Duration.ofMinutes(props.getRedis().getProductCacheTtlMinutes()));
        } catch (Exception e) {}

        return resp;
    }

    public List<String> getCategories() {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_CATEGORIES);
            if (cached != null) return objectMapper.convertValue(cached, new TypeReference<>() {});
        } catch (Exception e) {}
        List<String> categories = productRepo.findAllCategories();
        try { redisTemplate.opsForValue().set(CACHE_CATEGORIES, categories, Duration.ofHours(1)); }
        catch (Exception e) {}
        return categories;
    }

    /** Called by Kafka consumer when product sync event arrives from CRM */
    public void syncFromCrm(UUID crmProductId, String name, String description, String sku,
                            java.math.BigDecimal price, String unit, String imageUrl, boolean active) {
        var existing = productRepo.findByCrmProductId(crmProductId);
        if (existing.isPresent()) {
            Product p = existing.get();
            p.setName(name); p.setDescription(description); p.setSku(sku);
            p.setPrice(price); p.setUnit(unit);
            if (imageUrl != null) p.setImageUrl(imageUrl);
            p.setActive(active);
            productRepo.save(p);
            log.info("Product synced (updated): sku={}", sku);
        } else {
            Product p = Product.builder()
                .crmProductId(crmProductId).name(name).description(description)
                .sku(sku).price(price).unit(unit).imageUrl(imageUrl)
                .isActive(active).stockQuantity(100)
                .createdAt(java.time.Instant.now()).updatedAt(java.time.Instant.now())
                .build();
            productRepo.save(p);
            log.info("Product synced (created): sku={}", sku);
        }
        invalidateCache();
    }

    public void invalidateCache() {
        try {
            var keys = redisTemplate.keys("shop:product*");
            if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
            redisTemplate.delete(CACHE_CATEGORIES);
        } catch (Exception e) { log.debug("Cache invalidation error: {}", e.getMessage()); }
    }

    public Product findEntityById(UUID id) {
        return productRepo.findById(id).orElseThrow(() -> AppException.notFound("Товар"));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId()).name(p.getName()).description(p.getDescription())
            .sku(p.getSku()).price(p.getPrice()).unit(p.getUnit())
            .imageUrl(p.getImageUrl()).category(p.getCategory())
            .stockQuantity(p.getStockQuantity())
            .build();
    }

    private String blankToNull(String s) { return s != null && !s.isBlank() ? s : null; }
}
