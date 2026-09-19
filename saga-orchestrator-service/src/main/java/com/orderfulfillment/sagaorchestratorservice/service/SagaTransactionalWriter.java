package com.orderfulfillment.sagaorchestratorservice.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.orderfulfillment.sagaorchestratorservice.dto.OrderCreatedEvent;
import com.orderfulfillment.sagaorchestratorservice.dto.ReserveInventoryCommand;
import com.orderfulfillment.sagaorchestratorservice.entity.OutboxEvent;
import com.orderfulfillment.sagaorchestratorservice.entity.SagaState;
import com.orderfulfillment.sagaorchestratorservice.repository.OutboxEventRepository;
import com.orderfulfillment.sagaorchestratorservice.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaTransactionalWriter {

    private static final String RESERVE_INVENTORY_TOPIC = "inventory.reserve.command.v1";

    private final SagaStateRepository sagaStateRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void startSaga(OrderCreatedEvent event) {
        UUID sagaId = UUID.randomUUID();

        SagaState sagaState = new SagaState(sagaId, event.orderId(), SagaState.SagaStatus.STARTED);
        sagaStateRepository.save(sagaState);

        List<ReserveInventoryCommand.ItemPayload> items = event.items().stream()
                .map(item -> new ReserveInventoryCommand.ItemPayload(item.productId(), item.quantity()))
                .toList();

        ReserveInventoryCommand command = new ReserveInventoryCommand(sagaId, items);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JacksonException e) {
            throw new IllegalStateException("Échec de sérialisation de ReserveInventoryCommand pour sagaId=" + sagaId, e);
        }

        OutboxEvent outboxEvent = new OutboxEvent(sagaId, "ReserveInventoryCommand", RESERVE_INVENTORY_TOPIC, payload);
        outboxEventRepository.save(outboxEvent);
    }
}