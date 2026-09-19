package com.orderfulfillment.testtools.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        List<ItemPayload> items,
        BigDecimal totalAmount
) {
    public record ItemPayload(
            UUID productId,
            int quantity,
            BigDecimal unitPrice) {
    }
}