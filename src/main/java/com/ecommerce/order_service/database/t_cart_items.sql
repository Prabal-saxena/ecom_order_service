-- Updated cart_items table
CREATE TABLE order_service.t_cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_addition DOUBLE PRECISION NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart
        FOREIGN KEY (cart_id)
        REFERENCES order_service.t_cart_guest (id)
        ON DELETE CASCADE
);