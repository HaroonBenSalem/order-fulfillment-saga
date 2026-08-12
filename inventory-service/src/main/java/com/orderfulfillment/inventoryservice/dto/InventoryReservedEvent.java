package com.orderfulfillment.inventoryservice.dto;
import java.util.UUID;

public record InventoryReservedEvent(UUID sagaId) {}