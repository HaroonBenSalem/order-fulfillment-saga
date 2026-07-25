package com.haroun.order_service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderCreatedEvent {

    private UUID orderId;
    private UUID customerId;
    private List<ItemPayload> items;
    private BigDecimal totalAmount;

    public OrderCreatedEvent() {}
    public OrderCreatedEvent(UUID orderId, UUID customerId, List<ItemPayload> items, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public UUID getOrderId() { return orderId;}
    public UUID getCustomerId() { return customerId;}
    public List<ItemPayload> getItems() {return items;}
    public BigDecimal getTotalAmount() {return totalAmount;}

    public static class ItemPayload{
        private UUID productId;
        private int  quantity;
        private BigDecimal unitPrice;

        public ItemPayload() {}

        public ItemPayload(UUID productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        public UUID getProductId() { return productId;}
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }

}