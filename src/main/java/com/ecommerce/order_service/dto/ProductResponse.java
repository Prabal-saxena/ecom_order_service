package com.ecommerce.order_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Getter
@Setter
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private int quantity;
    private int rating;
    private String country;
    private String type;
    private float alcoholVol;
    private List<String> tags;
}
