package com.orderfulfillment.sagaorchestratorservice.dto;

import java.util.UUID;

public record InventoryReservedEvent(
        UUID sagaId
) {
}
