package com.zaalima.inventoryservice.listener;

import com.zaalima.inventoryservice.avro.OrderCreatedEvent;
import com.zaalima.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final InventoryService inventoryService;

    public OrderEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "order-events",
            containerFactory = "orderCreatedKafkaListenerContainerFactory",
            groupId = "inventory-service-avro"
    )
    public void consumeOrderEvent(OrderCreatedEvent orderEvent) {

        System.out.println(
                "Received order event: orderId="
                        + orderEvent.getOrderId()
                        + ", productId="
                        + orderEvent.getProductId()
                        + ", quantity="
                        + orderEvent.getQuantity()
                        + ", status="
                        + orderEvent.getStatus()
        );

        if ("CREATED".contentEquals(orderEvent.getStatus())) {
            inventoryService.processOrder(orderEvent);
        }
    }
}
