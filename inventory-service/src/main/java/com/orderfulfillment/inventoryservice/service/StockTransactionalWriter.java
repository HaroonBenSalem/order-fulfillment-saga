package com.orderfulfillment.inventoryservice.service;

import com.orderfulfillment.inventoryservice.entity.Stock;
import com.orderfulfillment.inventoryservice.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockTransactionalWriter{
    private final StockRepository stockRepository;
    @Transactional
    public void doReverse(String productId, int quantity){
        Stock stock = stockRepository.findByProductId(productId).orElseThrow(() -> new EntityNotFoundException("Stock introuvable: " + productId));
        if (stock.getQuantitySellable() < quantity){
            throw new IllegalStateException("Stock insuffisant pour " + productId);
        }

        stock.setQuantityReserved(stock.getQuantityReserved() + quantity);
        stockRepository.save(stock);
    }
}