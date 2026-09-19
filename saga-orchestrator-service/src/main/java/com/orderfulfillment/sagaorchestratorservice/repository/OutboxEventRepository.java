package com.orderfulfillment.sagaorchestratorservice.repository;

import com.orderfulfillment.sagaorchestratorservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedFalse();
}
