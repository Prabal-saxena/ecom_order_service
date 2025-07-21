package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.AddToCartRequest;
import com.ecommerce.order_service.dto.CartCountResponse;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.model.GuestCart;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.GuestCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final GuestCartRepository guestCartRepository;

    private final CartItemRepository cartItemRepository;

    private static final int GUEST_CART_EXPIRY_DAYS = 90;
    private static final Double price = 40.34;

    public GuestCart createGuestCart(){
        UUID newGuestCartId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plus(GUEST_CART_EXPIRY_DAYS, ChronoUnit.DAYS);
        GuestCart guestCart = new GuestCart(newGuestCartId, expiresAt);
        return guestCartRepository.save(guestCart);
    }

    public CartItem addProductToCart(UUID guestCartId, AddToCartRequest request){
        // First, ensure the guest cart exists and update its activity timestamp
        GuestCart guestCart = guestCartRepository.findById(guestCartId)
                .orElseThrow(() -> new RuntimeException("Guest cart not found: " + guestCartId));
        guestCart.setLastActivityAt(LocalDateTime.now());
        guestCartRepository.save(guestCart);

        // ToDo: Make a call to Product service to fetch price of product in real time.

        // Checking if this same product is already present in Database for this same guest
        Optional<CartItem> existingItem = cartItemRepository.findByGuestCartIdAndProductId(guestCartId, request.getProductId());

        CartItem cartItem;
        if(existingItem.isPresent()){
            cartItem = existingItem.get();
            cartItem.setQuantity(existingItem.get().getQuantity() + 1);
            cartItem.setPriceAtAddition(price + price);
        }else {
            cartItem = new CartItem(guestCartId, request.getProductId(), request.getQuantity(), price);
        }
        return cartItemRepository.save(cartItem);
    }

    public long getCartItemCount(UUID guestCartId){
        guestCartRepository.findById(guestCartId).ifPresent(gc-> {
            gc.setLastActivityAt(LocalDateTime.now());
            guestCartRepository.save(gc);
        });
        return cartItemRepository.findByGuestCartId(guestCartId).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void removeCartItem(UUID guestCartId, UUID cartItemId){

        boolean result = cartItemRepository.findByIdAndGuestCartId(cartItemId, guestCartId);
        if (!result) {
            throw new RuntimeException("Product couldn't be removed, please try again.");
        }
        cartItemRepository.deleteById(cartItemId);
    }
}
