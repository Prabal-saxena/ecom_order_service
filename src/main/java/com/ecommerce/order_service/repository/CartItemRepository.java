package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductId(UUID guestCartId, String productId);

    List<CartItem> findByCartId(UUID guestCartId);
}
