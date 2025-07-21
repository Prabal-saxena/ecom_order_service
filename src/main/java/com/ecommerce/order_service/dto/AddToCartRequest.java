package com.ecommerce.order_service.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AddToCartRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
