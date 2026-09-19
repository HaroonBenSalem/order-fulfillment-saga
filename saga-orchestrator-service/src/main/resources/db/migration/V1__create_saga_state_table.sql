CREATE TABLE saga_state(
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_saga_state_order_id ON saga_state(order_id);