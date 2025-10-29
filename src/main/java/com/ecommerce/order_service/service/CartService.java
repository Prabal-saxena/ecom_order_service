package com.ecommerce.order_service.service;

import com.ecommerce.order_service.config.WebClientConfig;
import com.ecommerce.order_service.dto.AddToCartRequest;
import com.ecommerce.order_service.dto.CartItemResponse;
import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.model.GuestCart;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.GuestCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final GuestCartRepository guestCartRepository;
    private final CartItemRepository cartItemRepository;
    private final WebClient.Builder webClientBuilder;
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

        ProductResponse product = webClientBuilder.build().get()
                .uri("http://34.31.139.23/api/product/id",
                        uriBuilder -> uriBuilder.queryParam("productId", request.getProductId()).build())
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();

        if(product == null)
            throw new RuntimeException("Unable to retrieve info from product service for productId: " + request.getProductId());

        // Checking if this same product is already present in Database for this same guest
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(guestCartId, request.getProductId());

        CartItem cartItem;
        if(existingItem.isPresent()){
            cartItem = existingItem.get();
            cartItem.setQuantity(existingItem.get().getQuantity() + 1);
            cartItem.setPriceAtAddition(product.getPrice() * cartItem.getQuantity());
        }else {
            cartItem = new CartItem(guestCartId, request.getProductId(), request.getQuantity(), product.getPrice());
        }
        return cartItemRepository.save(cartItem);
    }

    public long getCartItemCount(UUID guestCartId){
        guestCartRepository.findById(guestCartId).ifPresent(gc-> {
            gc.setLastActivityAt(LocalDateTime.now());
            guestCartRepository.save(gc);
        });
        return cartItemRepository.findByCartId(guestCartId).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getTotalPrice(UUID guestCartId, String productId) {
        if(guestCartId != null && !productId.equals("null")){
            CartItem cartItem = cartItemRepository.findByCartIdAndProductId(guestCartId, productId)
                    .orElseThrow(() -> new RuntimeException("Couldn't fetch the total price for product: " + productId));
            return cartItem.getPriceAtAddition();
        }
        if(guestCartId != null) {
            List<CartItem> cartItem = cartItemRepository.findByCartId(guestCartId);
            return cartItem.stream().mapToDouble(CartItem::getPriceAtAddition).sum();
        }else
            throw new RuntimeException("GuestID is not available in the database.");
    }

    public List<CartItemResponse> getCartItems(UUID guestCartId){
        // Query cartItems DB to fetch cart items for guestCartId
        // Gather Product info for each product available in the list.
        return mapCartItemAndProductInfo(cartItemRepository.findByCartId(guestCartId));
    }
    public void removeCartItem(UUID guestCartId, String productId, boolean completelyRemoveFlag){

        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(guestCartId, productId);
        if (!result.isPresent()) {
            throw new RuntimeException("Product couldn't be removed, please try again.");
        }

        ProductResponse product = webClientBuilder.build().get()
                .uri("http://34.31.139.23/api/product/id",
                        uriBuilder -> uriBuilder.queryParam("productId", productId).build())
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();

        if(result.get().getQuantity() == 1 || completelyRemoveFlag){
            cartItemRepository.deleteById(result.get().getId());
        }else {
            result.get().setQuantity(result.get().getQuantity() - 1);
            result.get().setPriceAtAddition(product.getPrice() * result.get().getQuantity());
            cartItemRepository.save(result.get());
        }
    }

    private List<CartItemResponse> mapCartItemAndProductInfo(List<CartItem> cartItemList){
        List<CartItemResponse> cartItemResponseList = new ArrayList<>();
        List<String> productIds = new ArrayList<>();

        cartItemList.stream().forEach(x-> productIds.add(x.getProductId()));

        if(productIds.isEmpty()){
            throw new RuntimeException("No product found in this cart!");
        }
        try {
            List<ProductResponse> productResponseList = webClientBuilder.build()
                    .post()
                    .uri("http://34.31.139.23/api/product/byIds")
                    .bodyValue(productIds)
                    .retrieve()
                    .bodyToFlux(ProductResponse.class)
                    .collectList()
                    .block();

            for (CartItem cartItem: cartItemList) {
                Optional<ProductResponse> product = productResponseList.stream().filter(x-> x.getId().equals(cartItem.getProductId())).findFirst();

                if (product.isPresent()){
                    CartItemResponse cartItemResponse = new CartItemResponse(
                            product.get().getId(),
                            product.get().getImageUrl(),
                            product.get().getName(),
                            product.get().getPrice(),
                            cartItem.getQuantity(),
                            product.get().getQuantity(),
                            product.get().getPrice() * cartItem.getQuantity()
                            );
                    cartItemResponseList.add(cartItemResponse);
                }else
                    throw new RuntimeException("Couldn't fetch product from productResponseList object.");
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            return new ArrayList<>();
        }
        return cartItemResponseList;
    }
}
