package com.orderfulfillment.inventoryservice.dto;

import  java.util.UUID;
import  java.util.List;

public record ReserveInventoryCommand(UUID sagaId, List<ItemPayload> items){
    public record ItemPayload(String productId, int quantity) {}
}
