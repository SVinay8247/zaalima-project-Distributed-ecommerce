package com.zaalima.inventoryservice.service;

import com.zaalima.inventoryservice.avro.OrderCreatedEvent;
import com.zaalima.inventoryservice.entity.Inventory;
import com.zaalima.inventoryservice.event.StockReservedEvent;
import com.zaalima.inventoryservice.event.StockReleasedEvent;
import com.zaalima.inventoryservice.repository.InventoryRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(
            InventoryRepository inventoryRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Optional<Inventory> updateInventory(
            Long id,
            Inventory updatedInventory) {

        return inventoryRepository.findById(id).map(inventory -> {
            inventory.setProductId(updatedInventory.getProductId());
            inventory.setQuantity(updatedInventory.getQuantity());
            return inventoryRepository.save(inventory);
        });
    }

    public boolean deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            return false;
        }

        inventoryRepository.deleteById(id);
        return true;
    }

    public void processOrder(OrderCreatedEvent orderEvent) {

        inventoryRepository.findByProductId(orderEvent.getProductId())
                .ifPresent(inventory -> {

                    int currentQuantity = inventory.getQuantity();
                    int orderedQuantity = orderEvent.getQuantity();

                    if (currentQuantity >= orderedQuantity) {

                        inventory.setQuantity(currentQuantity - orderedQuantity);

                        inventoryRepository.save(inventory);

                        StockReservedEvent stockReservedEvent =
                                new StockReservedEvent(
                                        orderEvent.getOrderId(),
                                        orderEvent.getProductId(),
                                        orderEvent.getQuantity(),
                                        "RESERVED"
                                );

                        kafkaTemplate.send(
                                "inventory-events",
                                String.valueOf(orderEvent.getOrderId()),
                                stockReservedEvent
                        );

                        System.out.println(
                                "Inventory reserved for order "
                                        + orderEvent.getOrderId()
                                        + ". Remaining quantity: "
                                        + inventory.getQuantity()
                        );

                    } else {

                        System.out.println(
                                "Insufficient inventory for product "
                                        + orderEvent.getProductId()
                        );
                    }
                });
    }

    public void releaseStock(StockReleasedEvent event) {

        inventoryRepository.findByProductId(event.getProductId())
                .ifPresent(inventory -> {

                    int currentQuantity = inventory.getQuantity();
                    int releasedQuantity = event.getQuantity();

                    inventory.setQuantity(currentQuantity + releasedQuantity);

                    inventoryRepository.save(inventory);

                    System.out.println(
                            "Inventory released for order "
                                    + event.getOrderId()
                                    + ". Restored quantity: "
                                    + inventory.getQuantity()
                    );
                });
    }
}
