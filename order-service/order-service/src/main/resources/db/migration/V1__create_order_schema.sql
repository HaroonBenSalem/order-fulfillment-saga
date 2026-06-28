-- Table: orders
-- Owned entirely by order-service. No FK to other services' data (database-per-service).
CREATE TABLE orders (
                        id            UUID PRIMARY KEY,
                        customer_id   UUID NOT NULL,
                        status        VARCHAR(20) NOT NULL CHECK (status IN ('VALIDATED', 'REJECTED')),
                        total_amount  NUMERIC(10, 2) NOT NULL,
                        created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Table: order_items
-- Belongs to the same domain as orders, hence a real FK to orders(id).
-- product_id references inventory-service's catalog — no FK (cross-service boundary).
CREATE TABLE order_items (
                             id          UUID PRIMARY KEY,
                             order_id    UUID NOT NULL REFERENCES orders(id),
                             product_id  UUID NOT NULL,
                             quantity    INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price  NUMERIC(10, 2) NOT NULL
);

-- Table: outbox_event
-- Implements the Transactional Outbox Pattern.
-- Acts as a generic message queue table — intentionally no FK to orders,
-- to keep it decoupled from the domain schema.
CREATE TABLE outbox_event (
                              id              UUID PRIMARY KEY,
                              aggregate_type  VARCHAR(50) NOT NULL,
                              aggregate_id    UUID NOT NULL,
                              event_type      VARCHAR(100) NOT NULL,
                              payload         JSONB NOT NULL,
                              created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                              published       BOOLEAN NOT NULL DEFAULT false,
                              published_at    TIMESTAMPTZ
);

-- Index to speed up the OutboxPublisher's polling query (WHERE published = false)
CREATE INDEX idx_outbox_event_published ON outbox_event (published);