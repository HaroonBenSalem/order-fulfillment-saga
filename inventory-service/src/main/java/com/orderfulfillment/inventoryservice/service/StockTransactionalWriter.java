package com.orderfulfillment.inventoryservice.service;

import com.orderfulfillment.inventoryservice.entity.Stock;
import com.orderfulfillment.inventoryservice.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.orderfulfillment.inventoryservice.dto.ReserveInventoryCommand;
import com.orderfulfillment.inventoryservice.entity.OutboxEvent;
import com.orderfulfillment.inventoryservice.repository.OutboxEventRepository;
import com.orderfulfillment.inventoryservice.dto.InventoryReservedEvent;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransactionalWriter{
    private final StockRepository stockRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper = new JsonMapper();
    private OutboxEvent buildOutboxEvent(UUID sagaId) {
        InventoryReservedEvent payloadObject = new InventoryReservedEvent(sagaId);
        String payloadJson = jsonMapper.writeValueAsString(payloadObject);

        OutboxEvent event = new OutboxEvent();
        event.setSagaId(sagaId);
        event.setEventType("InventoryReserved");
        event.setPayload(payloadJson);
        return event;
    }
    @Transactional
    public void processReservation(ReserveInventoryCommand command){
        for (var item : command.items()){
            Stock stock = stockRepository.findByProductId(item.productId()).orElseThrow(() -> new EntityNotFoundException("Stock introuvable: " + item.productId()));
            if (stock.getQuantitySellable() < item.quantity()){
                throw new IllegalStateException("Stock insuffisant pour " + item.productId());
            }

            stock.setQuantityReserved(stock.getQuantityReserved() + item.quantity());
            stockRepository.save(stock);
        }
        OutboxEvent event = buildOutboxEvent(command.sagaId());
        outboxEventRepository.save(event);
    }

}