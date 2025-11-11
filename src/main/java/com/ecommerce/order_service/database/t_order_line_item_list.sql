-- Join table automatically created by JPA for @OneToMany without mappedBy
CREATE TABLE order_service.t_orders_order_line_items_list (
    order_id BIGINT NOT NULL,
    order_line_items_list_id BIGINT NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id)
        REFERENCES order_service.t_orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_line_item FOREIGN KEY (order_line_items_list_id)
        REFERENCES order_service.t_order_line_items (id)
        ON DELETE CASCADE,
    PRIMARY KEY (order_id, order_line_items_list_id)
);