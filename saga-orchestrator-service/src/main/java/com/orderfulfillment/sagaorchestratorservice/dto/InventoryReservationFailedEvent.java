package com.orderfulfillment.sagaorchestratorservice.dto;

import java.util.UUID;

public record InventoryReservationFailedEvent(UUID sagaId, FailureReason reason) {}