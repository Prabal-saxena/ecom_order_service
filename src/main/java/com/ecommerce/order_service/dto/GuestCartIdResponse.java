package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.repository.GuestCartRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class GuestCartIdResponse {

    private String guestCartId;

    public GuestCartIdResponse(String guestCartId) {
        this.guestCartId = guestCartId;
    }
}
