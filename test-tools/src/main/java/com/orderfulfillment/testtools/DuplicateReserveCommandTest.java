package com.orderfulfillment.testtools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.List;

public class DuplicateReserveCommandTest {

    private static final String TOPIC = "inventory.reserve.command.v1";
    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        UUID sagaId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Map<String, Object> item = Map.of(
                "productId", PRODUCT_ID,
                "quantity", 1
        );

        Map<String, Object> command = Map.of(
                "sagaId", sagaId.toString(),
                "orderId", orderId.toString(),
                "items", List.of(item)
        );
        String json = new ObjectMapper().writeValueAsString(command);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 2; i++) {
                producer.send(new ProducerRecord<>(TOPIC, sagaId.toString(), json)).get();
                System.out.println("Envoi #" + i + " | sagaId=" + sagaId);
            }
        }
        System.out.println("Termine. Copie ce sagaId pour les requetes SQL: " + sagaId);
    }
}