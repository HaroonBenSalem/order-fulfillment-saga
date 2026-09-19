package com.orderfulfillment.sagaorchestratorservice.repository;

import com.orderfulfillment.sagaorchestratorservice.entity.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SagaStateRepository extends JpaRepository<SagaState, Long> {
    Optional<SagaState> findBySagaId(UUID sagaId);
}