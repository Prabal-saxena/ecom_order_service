CREATE TABLE IF NOT EXISTS order_service.t_cart
(
    id uuid PRIMARY KEY,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone,
    last_activity_at timestamp(6) without time zone NOT NULL,
	guest_cart_id uuid,
    user_cart_id uuid,
    CONSTRAINT t_cart_guest_user_cart_id_key UNIQUE (user_cart_id),
    CONSTRAINT t_cart_guest_user_cart_id_fkey FOREIGN KEY (user_cart_id)
        REFERENCES users.t_users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)