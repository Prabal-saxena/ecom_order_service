-- Optional: Table to manage guest cart metadata (e.g., last activity, creation time)
CREATE TABLE order_service.t_cart_guest (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Add a field for expiration date for cleanup (e.g., 90 days)
    expires_at TIMESTAMP WITH TIME ZONE
);