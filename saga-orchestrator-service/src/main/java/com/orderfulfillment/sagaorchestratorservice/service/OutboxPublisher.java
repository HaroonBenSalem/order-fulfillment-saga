package com.orderfulfillment.saga-orchestrator-service.service;

import com.orderfulfillment.saga-orchestrator-service.entity.OutboxEvent;
import com.orderfulfillment.saga-orchestrator-service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedFalse();

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.setPublished(true);
                outboxEventRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Publication interrompue pour l'event {}", event.getId(), e);
            } catch (Exception e) {
                log.error("Échec de publication pour l'event {} sur le topic {}", event.getId(), event.getTopic(), e);
            }
        }
    }
}
