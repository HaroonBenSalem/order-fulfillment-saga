package com.haroun.order_service.service;

import com.haroun.order_service.dto.CreateOrderRequest;
import com.haroun.order_service.dto.OrderCreatedEvent;
import com.haroun.order_service.entity.Order;
import com.haroun.order_service.entity.OrderItem;
import com.haroun.order_service.entity.OutboxEvent;
import com.haroun.order_service.repository.OrderRepository;
import com.haroun.order_service.repository.OutboxEventRepository;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public UUID createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(request.getCustomerId());
        order.setStatus("VALIDATED");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderCreatedEvent.ItemPayload> eventItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            orderItems.add(item);

            BigDecimal lineTotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            eventItems.add(new OrderCreatedEvent.ItemPayload(
                    itemReq.getProductId(), itemReq.getQuantity(), itemReq.getUnitPrice()));
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, request.getCustomerId(), eventItems, totalAmount);
        String payload = jsonMapper.writeValueAsString(event);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(orderId);
        outboxEvent.setAggregateType("Order");
        outboxEvent.setEventType("OrderCreated");
        outboxEvent.setPayload(payload);
        outboxEvent.setPublished(false);
        outboxEvent.setCreatedAt(now);

        orderRepository.save(order);
        outboxEventRepository.save(outboxEvent);

        return orderId;
    }
}