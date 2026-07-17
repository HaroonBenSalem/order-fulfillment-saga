package com.haroun.order_service.controller;
import com.haroun.order_service.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return UUID.randomUUID();
    }
}