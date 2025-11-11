-- Main Orders table
CREATE TABLE order_service.t_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(255)
);