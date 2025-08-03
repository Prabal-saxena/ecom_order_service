package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.AddToCartRequest;
import com.ecommerce.order_service.dto.CartCountResponse;
import com.ecommerce.order_service.dto.CartItemResponse;
import com.ecommerce.order_service.dto.GuestCartIdResponse;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.model.GuestCart;
import com.ecommerce.order_service.service.CartService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("guest_cart/create")
    public ResponseEntity<GuestCartIdResponse> createGuestCart(){
        GuestCart guestCart = cartService.createGuestCart();
        return new ResponseEntity<>(new GuestCartIdResponse(guestCart.getId().toString()), HttpStatus.CREATED);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItem> addProductToCart(
            @RequestHeader("X-Guest-Cart-Id") UUID guestCartId,
            @Valid @RequestBody AddToCartRequest request){
        try{
            CartItem cartItem = cartService.addProductToCart(guestCartId, request);
            return new ResponseEntity<>(cartItem, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Todo: Method to return product info + cart info to cart.html page
    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @RequestHeader("X-Guest-Cart-Id") UUID guestCartId
    ){
        List<CartItemResponse> cartItemResponsesList = cartService.getCartItems(guestCartId);
        return new ResponseEntity<>(cartItemResponsesList, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<CartCountResponse> getCartItemsCount(
            @RequestHeader("X-Guest-Cart-Id") UUID guestCartId
    ){
        long count = cartService.getCartItemCount(guestCartId);
        return ResponseEntity.ok(new CartCountResponse(count));
    }

    @GetMapping("/price")
    public double getTotalPrice(
            @RequestHeader("X-Guest-Cart-Id") UUID guestCartId,
            @RequestParam(required = false, defaultValue = "null") String productId
    ){
        return cartService.getTotalPrice(guestCartId, productId);
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> removeCartItem(
            @RequestHeader("X-Guest-Cart-Id") UUID guestCartId,
            @RequestParam String productId,
            @RequestParam String completeRemove
    ){
        cartService.removeCartItem(guestCartId,productId, Boolean.parseBoolean(completeRemove));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
