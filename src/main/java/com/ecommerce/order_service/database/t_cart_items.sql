-- Updated cart_items table
CREATE TABLE order_service.t_cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_cart_id UUID NOT NULL, -- New field to identify guest carts
    product_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price_at_addition NUMERIC(10, 2) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (guest_cart_id, product_id) -- A guest cart can only have one entry per product
);