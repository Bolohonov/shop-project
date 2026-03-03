package com.shop.product.repository;

import com.shop.product.entity.Product;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends CrudRepository<Product, UUID> {
    @Query("""
        SELECT * FROM products
        WHERE is_active = true
          AND (:query IS NULL OR name ILIKE '%' || :query || '%'
               OR description ILIKE '%' || :query || '%'
               OR sku ILIKE '%' || :query || '%')
          AND (:category IS NULL OR category = :category)
        ORDER BY
          CASE WHEN :sortBy = 'price_asc' THEN price END ASC,
          CASE WHEN :sortBy = 'price_desc' THEN price END DESC,
          CASE WHEN :sortBy = 'name_asc' THEN name END ASC,
          CASE WHEN :sortBy = 'name_desc' THEN name END DESC,
          name ASC
        LIMIT :lim OFFSET :off
        """)
    List<Product> search(String query, String category, String sortBy, int lim, int off);

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE is_active = true
          AND (:query IS NULL OR name ILIKE '%' || :query || '%'
               OR description ILIKE '%' || :query || '%'
               OR sku ILIKE '%' || :query || '%')
          AND (:category IS NULL OR category = :category)
        """)
    long countSearch(String query, String category);

    Optional<Product> findBySku(String sku);
    Optional<Product> findByCrmProductId(UUID crmProductId);

    @Query("SELECT DISTINCT category FROM products WHERE is_active = true AND category IS NOT NULL ORDER BY category")
    List<String> findAllCategories();
}
