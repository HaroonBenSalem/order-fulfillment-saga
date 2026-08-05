package com.orderfulfillment.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "stock")
@Getter
@Setter
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at",nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Transient
    public int getQuantitySellable() {
        return quantityAvailable - quantityReserved;
    }
}
