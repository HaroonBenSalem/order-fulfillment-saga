package com.orderfulfillment.inventoryservice.kafka;

import com.orderfulfillment.inventoryservice.dto.ReserveInventoryCommand;
import com.orderfulfillment.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j

public class InventoryCommandListener {
    private final StockReservationService stockReservationService;
    private final JsonMapper jsonMapper = new JsonMapper();

    @KafkaListener(topics = "inventory.reserve.command.v1")
    public void handleReserveCommand(String message){
        ReserveInventoryCommand command = jsonMapper.readValue(message, ReserveInventoryCommand.class);
        log.info("Received reservation command for saga {}", command.sagaId());
        stockReservationService.reserveForCommand(command);
    }

}
