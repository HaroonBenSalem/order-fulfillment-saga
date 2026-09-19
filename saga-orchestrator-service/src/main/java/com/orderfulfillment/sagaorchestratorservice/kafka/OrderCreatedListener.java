package com.orderfulfillment.sagaorchestratorservice.kafka;

import com.orderfulfillment.sagaorchestratorservice.dto.OrderCreatedEvent;
import com.orderfulfillment.sagaorchestratorservice.service.SagaTransactionalWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final SagaTransactionalWriter sagaTransactionalWriter;
    private final JsonMapper jsonMapper = new JsonMapper();

    @KafkaListener(topics = "order.created.v1")
    public void handle(String rawJson) {
        OrderCreatedEvent event = jsonMapper.readValue(rawJson, OrderCreatedEvent.class);
        sagaTransactionalWriter.startSaga(event);
    }
}