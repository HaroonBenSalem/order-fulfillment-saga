package com.haroun.order_service.controller;
import com.haroun.order_service.dto.CreateOrderRequest;
import com.haroun.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<UUID> createOrder(@RequestBody @Valid CreateOrderRequest request) {

        UUID orderId = orderService.createOrder(request);
        URI location = URI.create("/api/orders/" + orderId);
        return ResponseEntity.created(location).body(orderId);

    }
}