package com.orderfulfillment.testtools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderfulfillment.testtools.dto.OrderCreatedEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;


import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class OrderCreatedTestProducer {

    private static final String TOPIC = "order.created.v1";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID customerId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                customerId,
                List.of(
                        new OrderCreatedEvent.ItemPayload(productId, 2, new BigDecimal("19.99"))
                ),
                new BigDecimal("39.98")
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String payloadJson = objectMapper.writeValueAsString(event);

        System.out.println("Payload JSON à envoyer : " + payloadJson);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC, orderId.toString(), payloadJson);

            RecordMetadata metadata = producer.send(record).get();

            System.out.println("Envoyé avec succès -> partition=" + metadata.partition()
                    + ", offset=" + metadata.offset());
        }
    }
}