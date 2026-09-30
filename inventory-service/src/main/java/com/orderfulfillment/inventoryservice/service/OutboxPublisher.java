package com.orderfulfillment.inventoryservice.service;

import com.orderfulfillment.inventoryservice.entity.OutboxEvent;
import com.orderfulfillment.inventoryservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Map<String, String> TOPIC_BY_EVENT_TYPE = Map.of(
            "InventoryReserved", "inventory.reserved.v1",
            "InventoryReservationFailed", "inventory.reservation-failed.v1"
    );


    @Scheduled(fixedDelay = 500)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedFalse();

        for (OutboxEvent event : pendingEvents) {
            String topic = TOPIC_BY_EVENT_TYPE.get(event.getEventType());
            if (topic == null) {
                log.error("Unknown eventType {} for outbox event {}, skipping", event.getEventType(), event.getId());
                continue;
            }
            try {
                kafkaTemplate.send(topic, event.getPayload()).get();
                event.setPublished(true);
                outboxEventRepository.save(event);
                log.info("Published outbox event {} for saga {}", event.getId(), event.getSagaId());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to publish outbox event {} for saga {}", event.getId(), event.getSagaId(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}