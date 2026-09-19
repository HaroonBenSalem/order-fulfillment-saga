CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(150) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbox_event_unpublished ON outbox_event (id) WHERE published = false;