package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}