package com.zaalima.inventoryservice.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaalima.inventoryservice.event.StockReleasedEvent;
import com.zaalima.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReleasedListener {

    private final InventoryService inventoryService;

    public StockReleasedListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "inventory-events",
            containerFactory = "stockReleasedKafkaListenerContainerFactory",
            groupId = "inventory-release-group"
    )
    public void handleStockReleased(JsonNode event) {

        if (event == null || event.get("status") == null) {
            return;
        }

        if ("RELEASED".equalsIgnoreCase(event.get("status").asText())) {

            if (event.get("orderId") == null
                    || event.get("productId") == null
                    || event.get("quantity") == null) {
                return;
            }

            StockReleasedEvent releasedEvent =
                    new StockReleasedEvent(
                            event.get("orderId").asLong(),
                            event.get("productId").asLong(),
                            event.get("quantity").asInt(),
                            event.get("status").asText()
                    );

            inventoryService.releaseStock(releasedEvent);
        }
    }
}
