package com.ecommerce.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "t_cart_items", schema = "order_service")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cartId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Double priceAtAddition;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    public CartItem(){
        this.addedAt = LocalDateTime.now();
    }

    public CartItem(UUID cartId, String productId, int quantity, Double priceAtAddition) {
        this();
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
    }
}
