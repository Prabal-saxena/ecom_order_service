package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.AddToCartRequest;
import com.ecommerce.order_service.dto.CartItemResponse;
import com.ecommerce.order_service.dto.ProductResponse;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WebClient.Builder webClientBuilder;
    private static final int GUEST_CART_EXPIRY_DAYS = 30;

    @Value("${PRODUCT_SERVICE_URL}")
    private String productServiceUrl;

    public Cart createGuestCart(){
        UUID newCartId = UUID.randomUUID();
        UUID newGuestCartId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plus(GUEST_CART_EXPIRY_DAYS, ChronoUnit.DAYS);
        Cart cart = new Cart(newCartId, newGuestCartId, expiresAt);
        return cartRepository.save(cart);
    }

    public CartItem addProductToCart(UUID cartId, AddToCartRequest request){
        // First, ensure the cart exists and update its activity timestamp
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found: " + cartId));
        cart.setLastActivityAt(LocalDateTime.now());
        cartRepository.save(cart);

        ProductResponse product = webClientBuilder.build().get()
                .uri(productServiceUrl + "/id",
                        uriBuilder -> uriBuilder.queryParam("productId", request.getProductId()).build())
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();

        if(product == null)
            throw new RuntimeException("Unable to retrieve info from product service for productId: " + request.getProductId());

        // Checking if this same product is already present in Database for this same guest
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId());

        CartItem cartItem;
        if(existingItem.isPresent()){
            cartItem = existingItem.get();
            cartItem.setQuantity(existingItem.get().getQuantity() + 1);
            cartItem.setPriceAtAddition(product.getPrice() * cartItem.getQuantity());
        }else {
            cartItem = new CartItem(cartId, request.getProductId(), request.getQuantity(), product.getPrice());
        }
        return cartItemRepository.save(cartItem);
    }

    public long getCartItemCount(UUID cartId){
        cartRepository.findById(cartId).ifPresent(gc-> {
            gc.setLastActivityAt(LocalDateTime.now());
            cartRepository.save(gc);
        });
        return cartItemRepository.findByCartId(cartId).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getTotalPrice(UUID cartId, String productId) {
        if(cartId != null && !productId.equals("null")){
            CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                    .orElseThrow(() -> new RuntimeException("Couldn't fetch the total price for product: " + productId));
            return cartItem.getPriceAtAddition();
        }
        if(cartId != null) {
            List<CartItem> cartItem = cartItemRepository.findByCartId(cartId);
            return cartItem.stream().mapToDouble(CartItem::getPriceAtAddition).sum();
        }else
            throw new RuntimeException("GuestID is not available in the database.");
    }

    public List<CartItemResponse> getCartItems(UUID cartId){
        // Query cartItems DB to fetch cart items for guestCartId
        // Gather Product info for each product available in the list.
        return mapCartItemAndProductInfo(cartItemRepository.findByCartId(cartId));
    }
    public void removeCartItem(UUID guestCartId, String productId, boolean completelyRemoveFlag){

        Optional<CartItem> result = cartItemRepository.findByCartIdAndProductId(guestCartId, productId);
        if (!result.isPresent()) {
            throw new RuntimeException("Product couldn't be removed, please try again.");
        }

        ProductResponse product = webClientBuilder.build().get()
                .uri(productServiceUrl + "/id",
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
                    .uri(productServiceUrl + "/byIds")
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
