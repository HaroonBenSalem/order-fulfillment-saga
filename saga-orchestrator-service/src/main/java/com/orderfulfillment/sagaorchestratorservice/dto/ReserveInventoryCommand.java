package com.orderfulfillment.sagaorchestratorservice.dto;

import java.util.List;
import java.util.UUID;

public record ReserveInventoryCommand(
        UUID sagaId,
        List<ItemPayload> items
) {
    public record ItemPayload(
            UUID productId,
            int quantity
    ) {
    }
}