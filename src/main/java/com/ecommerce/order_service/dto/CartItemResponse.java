package com.ecommerce.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class CartItemResponse {

    private String productId;
    private String productImgUrl;
    private String productName;
    private double productPrice;
    private int count;
    private int quantity;
    private double totalPrice;
}
