package com.orderfulfillment.inventoryservice.dto;
import java.util.UUID;

public record InventoryReservationFailedEvent(UUID sagaId, FailureReason reason) {}