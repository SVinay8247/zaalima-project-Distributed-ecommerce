package com.zaalima.inventoryservice.controller;

import com.zaalima.inventoryservice.entity.Inventory;
import com.zaalima.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void getAllInventory_shouldReturnInventoryList() {
        Inventory inventory = new Inventory(101L, 50);

        when(inventoryService.getAllInventory())
                .thenReturn(List.of(inventory));

        List<Inventory> result = inventoryController.getAllInventory();

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getProductId());
        assertEquals(50, result.get(0).getQuantity());

        verify(inventoryService).getAllInventory();
    }

    @Test
    void getInventoryById_shouldReturnInventory() {
        Inventory inventory = new Inventory(101L, 50);

        when(inventoryService.getInventoryById(1L))
                .thenReturn(Optional.of(inventory));

        ResponseEntity<Inventory> response =
                inventoryController.getInventoryById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getProductId());
        assertEquals(50, response.getBody().getQuantity());

        verify(inventoryService).getInventoryById(1L);
    }

    @Test
    void getInventoryById_whenNotFound_shouldReturn404() {
        when(inventoryService.getInventoryById(99L))
                .thenReturn(Optional.empty());

        ResponseEntity<Inventory> response =
                inventoryController.getInventoryById(99L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(inventoryService).getInventoryById(99L);
    }

    @Test
    void createInventory_shouldReturn201() {
        Inventory inventory = new Inventory(102L, 100);

        when(inventoryService.createInventory(inventory))
                .thenReturn(inventory);

        ResponseEntity<Inventory> response =
                inventoryController.createInventory(inventory);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(102L, response.getBody().getProductId());
        assertEquals(100, response.getBody().getQuantity());

        verify(inventoryService).createInventory(inventory);
    }

    @Test
    void updateInventory_shouldReturnUpdatedInventory() {
        Inventory inventory = new Inventory(101L, 75);

        when(inventoryService.updateInventory(1L, inventory))
                .thenReturn(Optional.of(inventory));

        ResponseEntity<Inventory> response =
                inventoryController.updateInventory(1L, inventory);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(75, response.getBody().getQuantity());

        verify(inventoryService).updateInventory(1L, inventory);
    }

    @Test
    void deleteInventory_shouldReturn204() {
        when(inventoryService.deleteInventory(1L))
                .thenReturn(true);

        ResponseEntity<Void> response =
                inventoryController.deleteInventory(1L);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(inventoryService).deleteInventory(1L);
    }

    @Test
    void deleteInventory_whenNotFound_shouldReturn404() {
        when(inventoryService.deleteInventory(99L))
                .thenReturn(false);

        ResponseEntity<Void> response =
                inventoryController.deleteInventory(99L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(inventoryService).deleteInventory(99L);
    }
}
