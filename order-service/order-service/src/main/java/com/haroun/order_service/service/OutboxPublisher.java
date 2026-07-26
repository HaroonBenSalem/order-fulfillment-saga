package com.haroun.order_service.service;

import com.haroun.order_service.entity.OutboxEvent;
import com.haroun.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ExecutionException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents(){
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedFalse();
        for (OutboxEvent event : pendingEvents){
            String topic = "order.created.v1";
            String key = event.getAggregateId().toString();
            String value = event.getPayload();
            try{
                kafkaTemplate.send(topic, key, value).get();
                event.setPublished(true);
                outboxEventRepository.save(event);
            } catch(InterruptedException | ExecutionException e){
                Thread.currentThread().interrupt();
            }



        }
    }
}