package com.orderfulfillment.sagaorchestratorservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor

public class Outboxevent{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "saga_id", nullable = false )
    private UUID sagaId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "topic", nullable = false)
    private String topic;
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(name = "published", nullable = false)
    private boolean published = false;
    @Column(name = "create_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Outboxevent(UUID sagaId, String eventType, String topic, String payload, boolean published) {
        this.sagaId = sagaId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
    }
}