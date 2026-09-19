package com.orderfulfillment.sagaorchestratorservice.kafka;

import com.orderfulfillment.sagaorchestratorservice.dto.InventoryReservedEvent;
import com.orderfulfillment.sagaorchestratorservice.entity.SagaState;
import com.orderfulfillment.sagaorchestratorservice.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class InventoryReservedListener {

    private final SagaStateRepository sagaStateRepository;

    @KafkaListener(topics = "inventory.reserved.v1")
    @Transactional
    public void handle(String rawJson) {
        JsonMapper mapper = JsonMapper.builder().build();
        InventoryReservedEvent event = mapper.readValue(rawJson, InventoryReservedEvent.class);
        SagaState sagaState = sagaStateRepository.findBySagaId(event.sagaId())
                .orElseThrow(() -> new IllegalStateException(
                        "SagaState introuvable pour sagaId=" + event.sagaId()));

        sagaState.setStatus(SagaState.SagaStatus.COMPLETED);
        sagaStateRepository.save(sagaState);
    }
}