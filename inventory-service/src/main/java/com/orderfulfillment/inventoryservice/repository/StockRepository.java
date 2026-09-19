package com.orderfulfillment.inventoryservice.repository;

import com.orderfulfillment.inventoryservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(UUID productId);
}