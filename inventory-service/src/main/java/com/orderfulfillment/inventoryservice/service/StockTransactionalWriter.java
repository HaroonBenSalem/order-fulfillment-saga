package com.orderfulfillment.inventoryservice.service;

import com.orderfulfillment.inventoryservice.entity.Stock;
import com.orderfulfillment.inventoryservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.orderfulfillment.inventoryservice.dto.ReserveInventoryCommand;
import com.orderfulfillment.inventoryservice.dto.FailureReason;
import com.orderfulfillment.inventoryservice.entity.OutboxEvent;
import com.orderfulfillment.inventoryservice.repository.OutboxEventRepository;
import com.orderfulfillment.inventoryservice.dto.InventoryReservedEvent;
import com.orderfulfillment.inventoryservice.dto.InventoryReservationFailedEvent;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransactionalWriter {

    private final StockRepository stockRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper = new JsonMapper();

    @Transactional
    public void processReservation(ReserveInventoryCommand command) {
        for (var item : command.items()) {
            if (item.quantity() <= 0) {
                writeFailure(command.sagaId(), FailureReason.INVALID_QUANTITY);
                return;
            }
            var stockOpt = stockRepository.findByProductId(item.productId());
            if (stockOpt.isEmpty()) {
                writeFailure(command.sagaId(), FailureReason.PRODUCT_NOT_FOUND);
                return;
            }
            if (stockOpt.get().getQuantitySellable() < item.quantity()) {
                writeFailure(command.sagaId(), FailureReason.INSUFFICIENT_STOCK);
                return;
            }
        }
        for (var item : command.items()) {
            Stock stock = stockRepository.findByProductId(item.productId()).orElseThrow();
            stock.setQuantityReserved(stock.getQuantityReserved() + item.quantity());
            stockRepository.save(stock);
        }

        outboxEventRepository.save(
                buildOutboxEvent(command.sagaId(), "InventoryReserved",
                        new InventoryReservedEvent(command.sagaId()))
        );
    }
    private void writeFailure(UUID sagaId, FailureReason reason) {
        var payload = new InventoryReservationFailedEvent(sagaId, reason);
        outboxEventRepository.save(
                buildOutboxEvent(sagaId, "InventoryReservationFailed", payload)
        );
    }
    private OutboxEvent buildOutboxEvent(UUID sagaId, String eventType, Object payloadObject) {
        String payloadJson = jsonMapper.writeValueAsString(payloadObject);

        OutboxEvent event = new OutboxEvent();
        event.setSagaId(sagaId);
        event.setEventType(eventType);
        event.setPayload(payloadJson);
        return event;
    }
}