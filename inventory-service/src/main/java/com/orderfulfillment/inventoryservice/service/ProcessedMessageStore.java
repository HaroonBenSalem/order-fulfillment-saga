package com.orderfulfillment.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessedMessageStore {

    private final JdbcTemplate jdbcTemplate;

    public boolean markIfNew(UUID sagaId, String messageType) {
        int rows = jdbcTemplate.update("""
                INSERT INTO processed_message (saga_id, message_type)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, sagaId, messageType);
        return rows == 1;
    }
}