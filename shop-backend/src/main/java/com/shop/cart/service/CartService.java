package com.shop.cart.service;

import com.shop.cart.dto.AddToCartRequest;
import com.shop.cart.dto.CartItemResponse;
import com.shop.cart.dto.CartResponse;
import com.shop.cart.entity.CartItem;
import com.shop.cart.repository.CartRepository;
import com.shop.common.exception.AppException;
import com.shop.product.entity.Product;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepo;
    private final ProductService productService;

    public CartResponse getCart(UUID userId) {
        List<CartItem> items = cartRepo.findByUserId(userId);
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            Product p = productService.findEntityById(ci.getProductId());
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            itemResponses.add(CartItemResponse.builder()
                .productId(p.getId()).productName(p.getName()).productSku(p.getSku())
                .productImageUrl(p.getImageUrl()).price(p.getPrice()).unit(p.getUnit())
                .quantity(ci.getQuantity()).totalPrice(lineTotal)
                .build());
            total = total.add(lineTotal);
        }

        return CartResponse.builder()
            .items(itemResponses).totalAmount(total).totalItems(items.size()).build();
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddToCartRequest req) {
        productService.findEntityById(req.getProductId()); // validate product exists
        var existing = cartRepo.findByUserIdAndProductId(userId, req.getProductId());
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + req.getQuantity());
            cartRepo.save(ci);
        } else {
            cartRepo.save(CartItem.builder()
                .userId(userId).productId(req.getProductId())
                .quantity(req.getQuantity()).addedAt(Instant.now()).build());
        }
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(UUID userId, UUID productId, int quantity) {
        if (quantity <= 0) { removeItem(userId, productId); return getCart(userId); }
        CartItem ci = cartRepo.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> AppException.notFound("Товар в корзине"));
        ci.setQuantity(quantity);
        cartRepo.save(ci);
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID productId) {
        cartRepo.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
    }

    @Transactional
    public void clearCart(UUID userId) { cartRepo.deleteByUserId(userId); }

    public List<CartItem> getCartItems(UUID userId) { return cartRepo.findByUserId(userId); }
}
