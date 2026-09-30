package com.orderfulfillment.sagaorchestratorservice.kafka;

import com.orderfulfillment.sagaorchestratorservice.dto.InventoryReservationFailedEvent;
import com.orderfulfillment.sagaorchestratorservice.entity.SagaState;
import com.orderfulfillment.sagaorchestratorservice.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationFailedListener {

    private final SagaStateRepository sagaStateRepository;

    @KafkaListener(topics = "inventory.reservation-failed.v1")
    @Transactional
    public void handle(String rawJson) {
        JsonMapper mapper = JsonMapper.builder().build();
        InventoryReservationFailedEvent event = mapper.readValue(rawJson, InventoryReservationFailedEvent.class);

        SagaState sagaState = sagaStateRepository.findBySagaId(event.sagaId())
                .orElseThrow(() -> new IllegalStateException(
                        "SagaState introuvable pour sagaId=" + event.sagaId()));

        sagaState.setStatus(SagaState.SagaStatus.CANCELLED);
        sagaStateRepository.save(sagaState);
        log.info("Saga {} annulée — raison: {}", event.sagaId(), event.reason());
    }
}