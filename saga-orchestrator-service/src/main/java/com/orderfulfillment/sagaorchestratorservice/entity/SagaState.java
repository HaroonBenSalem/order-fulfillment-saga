package com.orderfulfillment.sagaorchestratorservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_state")
@Getter
@Setter
@NoArgsConstructor
public class SagaState{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "saga_id", nullable = false, unique = true)
    private UUID sagaId;
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "update_at", nullable = false)
    private Instant updateAt;

    public SagaState (UUID sagaId, UUID orderId, SagaStatus status){
        this.orderId = orderId;
        this.sagaId = sagaId;
        this.status = status;
    }

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
        this.updateAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updateAt = Instant.now();
    }

    public enum SagaStatus{
        STARTED,
        INVENTORY_RESERVATION_PENDING,
        INVENTORY_RESERVED,
        COMPLETED,
    }
}