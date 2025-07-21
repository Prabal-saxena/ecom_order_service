package com.ecommerce.order_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CartCountResponse {
    private long count;

    public CartCountResponse(long count){
        this.count = count;
    }
}
