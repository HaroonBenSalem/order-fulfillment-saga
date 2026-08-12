CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_event_published ON outbox_event (published) WHERE published = false;