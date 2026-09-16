package com.zaalima.inventoryservice.service;

import com.zaalima.inventoryservice.entity.Inventory;
import com.zaalima.inventoryservice.event.StockReleasedEvent;
import com.zaalima.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void getAllInventory_shouldReturnInventoryList() {
        Inventory inventory = new Inventory(101L, 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));

        List<Inventory> result = inventoryService.getAllInventory();

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getProductId());
        assertEquals(50, result.get(0).getQuantity());

        verify(inventoryRepository).findAll();
    }

    @Test
    void getInventoryById_shouldReturnInventory() {
        Inventory inventory = new Inventory(101L, 50);

        when(inventoryRepository.findById(1L))
                .thenReturn(Optional.of(inventory));

        Optional<Inventory> result = inventoryService.getInventoryById(1L);

        assertTrue(result.isPresent());
        assertEquals(101L, result.get().getProductId());
        assertEquals(50, result.get().getQuantity());

        verify(inventoryRepository).findById(1L);
    }

    @Test
    void createInventory_shouldSaveInventory() {
        Inventory inventory = new Inventory(102L, 100);

        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = inventoryService.createInventory(inventory);

        assertNotNull(result);
        assertEquals(102L, result.getProductId());
        assertEquals(100, result.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updateInventory_shouldUpdateExistingInventory() {
        Inventory existing = new Inventory(101L, 50);
        Inventory updated = new Inventory(101L, 75);

        when(inventoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(inventoryRepository.save(existing))
                .thenReturn(existing);

        Optional<Inventory> result =
                inventoryService.updateInventory(1L, updated);

        assertTrue(result.isPresent());
        assertEquals(101L, result.get().getProductId());
        assertEquals(75, result.get().getQuantity());

        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(existing);
    }

    @Test
    void deleteInventory_shouldDeleteExistingInventory() {
        when(inventoryRepository.existsById(1L)).thenReturn(true);

        boolean result = inventoryService.deleteInventory(1L);

        assertTrue(result);

        verify(inventoryRepository).existsById(1L);
        verify(inventoryRepository).deleteById(1L);
    }
    @Test
    void releaseStock_shouldRestoreInventoryQuantity() {
        Inventory inventory = new Inventory(101L, 50);

        StockReleasedEvent event =
                new StockReleasedEvent(1L, 101L, 10, "RELEASED");

        when(inventoryRepository.findByProductId(101L))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory))
                .thenReturn(inventory);

        inventoryService.releaseStock(event);

        assertEquals(60, inventory.getQuantity());

        verify(inventoryRepository).findByProductId(101L);
        verify(inventoryRepository).save(inventory);
    }
}
