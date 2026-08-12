package com.orderfulfillment.inventoryservice.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import com.orderfulfillment.inventoryservice.dto.ReserveInventoryCommand;

@Service
@RequiredArgsConstructor
public class StockReservationService {
    private final StockTransactionalWriter transactionalWriter;

    @Retryable(
            retryFor = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    public void reserveForCommand(ReserveInventoryCommand command){
        transactionalWriter.processReservation(command);
    }
}