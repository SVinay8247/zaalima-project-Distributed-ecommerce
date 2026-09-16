package com.zaalima.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaalima.orderservice.entity.Order;
import com.zaalima.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void getAllOrders_shouldReturnOrders() throws Exception {
        Order order1 = new Order();
        Order order2 = new Order();

        when(orderService.getAllOrders())
                .thenReturn(Arrays.asList(order1, order2));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() throws Exception {
        Order order = new Order();

        when(orderService.getOrderById(1L))
                .thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk());

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrderById_shouldReturnEmpty_whenNotExists() throws Exception {
        when(orderService.getOrderById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isOk());

        verify(orderService).getOrderById(99L);
    }

    @Test
    void createOrder_shouldCreateOrder() throws Exception {
        Order order = new Order();

        when(orderService.createOrder(any(Order.class)))
                .thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated());

        verify(orderService).createOrder(any(Order.class));
    }

    @Test
    void updateOrder_shouldUpdateOrder_whenExists() throws Exception {
        Order order = new Order();
        order.setStatus("SHIPPED");

        when(orderService.updateOrder(eq(1L), any(Order.class)))
                .thenReturn(Optional.of(order));

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());

        verify(orderService).updateOrder(eq(1L), any(Order.class));
    }

    @Test
    void updateOrder_shouldReturnEmpty_whenNotExists() throws Exception {
        Order order = new Order();
        order.setStatus("SHIPPED");

        when(orderService.updateOrder(eq(99L), any(Order.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/orders/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());

        verify(orderService).updateOrder(eq(99L), any(Order.class));
    }

    @Test
    void deleteOrder_shouldDeleteOrder() throws Exception {
        when(orderService.deleteOrder(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }

    @Test
    void deleteOrder_shouldReturnNoContent_whenOrderDoesNotExist() throws Exception {
        when(orderService.deleteOrder(99L))
                .thenReturn(false);

        mockMvc.perform(delete("/orders/99"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(99L);
    }
}
