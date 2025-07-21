package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.dto.GuestCartIdResponse;
import com.ecommerce.order_service.model.GuestCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestCartRepository extends JpaRepository<GuestCart, UUID> {
}