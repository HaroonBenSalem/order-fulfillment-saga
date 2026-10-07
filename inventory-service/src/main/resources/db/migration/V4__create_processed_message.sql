CREATE TABLE processed_message (
    saga_id UUID NOT NULL,
    message_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (saga_id, message_type)
);