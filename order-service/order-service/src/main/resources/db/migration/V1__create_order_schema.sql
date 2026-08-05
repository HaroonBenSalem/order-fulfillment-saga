CREATE TABLE orders (
                        id            UUID PRIMARY KEY,
                        customer_id   UUID NOT NULL,
                        status        VARCHAR(20) NOT NULL CHECK (status IN ('VALIDATED', 'REJECTED')),
                        total_amount  NUMERIC(10, 2) NOT NULL,
                        created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
                             id          UUID PRIMARY KEY,
                             order_id    UUID NOT NULL REFERENCES orders(id),
                             product_id  UUID NOT NULL,
                             quantity    INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price  NUMERIC(10, 2) NOT NULL
);

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

CREATE INDEX idx_outbox_event_published ON outbox_event (published);