package com.zaalima.orderservice.service;

import com.zaalima.orderservice.avro.OrderCreatedEvent;
import com.zaalima.orderservice.entity.Order;
import com.zaalima.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void setOrderId(Order order, Long id) throws Exception {
        Field field = Order.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(order, id);
    }

    @Test
    void getAllOrders_shouldReturnOrders() {
        Order order1 = new Order();
        Order order2 = new Order();

        when(orderRepository.findAll())
                .thenReturn(Arrays.asList(order1, order2));

        var result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        Order order = new Order();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertSame(order, result.get());

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_shouldReturnEmpty_whenNotExists() {
        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        var result = orderService.getOrderById(99L);

        assertTrue(result.isEmpty());

        verify(orderRepository).findById(99L);
    }

    @Test
    void createOrder_shouldSaveAndReturnOrder() throws Exception {
        Order order = new Order();
        order.setProductId(101L);
        order.setQuantity(2);
        order.setStatus("CREATED");

        setOrderId(order, 1L);

        when(orderRepository.save(order))
                .thenReturn(order);

        var result = orderService.createOrder(order);

        assertSame(order, result);

        verify(orderRepository).save(order);

        verify(kafkaTemplate).send(
                eq("order-events"),
                eq("1"),
                argThat(event ->
                        event.getOrderId() == 1L
                                && event.getProductId() == 101L
                                && event.getQuantity() == 2
                                && "CREATED".contentEquals(event.getStatus())
                )
        );
    }

    @Test
    void updateOrder_shouldUpdateStatus_whenExists() throws Exception {
        Order existingOrder = new Order();
        existingOrder.setProductId(101L);
        existingOrder.setQuantity(2);
        existingOrder.setStatus("CREATED");

        setOrderId(existingOrder, 1L);

        Order updatedOrder = new Order();
        updatedOrder.setStatus("SHIPPED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(existingOrder))
                .thenReturn(existingOrder);

        var result = orderService.updateOrder(1L, updatedOrder);

        assertTrue(result.isPresent());
        assertEquals("SHIPPED", result.get().getStatus());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(existingOrder);

        verify(kafkaTemplate).send(
                eq("order-events"),
                eq("1"),
                argThat(event ->
                        event.getOrderId() == 1L
                                && event.getProductId() == 101L
                                && event.getQuantity() == 2
                                && "SHIPPED".contentEquals(event.getStatus())
                )
        );
    }

    @Test
    void updateOrder_shouldReturnEmpty_whenNotExists() {
        Order updatedOrder = new Order();
        updatedOrder.setStatus("SHIPPED");

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        var result = orderService.updateOrder(99L, updatedOrder);

        assertTrue(result.isEmpty());

        verify(orderRepository).findById(99L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(
                anyString(),
                anyString(),
                any(OrderCreatedEvent.class)
        );
    }

    @Test
    void deleteOrder_shouldDelete_whenExists() {
        when(orderRepository.existsById(1L))
                .thenReturn(true);

        boolean result = orderService.deleteOrder(1L);

        assertTrue(result);

        verify(orderRepository).existsById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrder_shouldNotDelete_whenNotExists() {
        when(orderRepository.existsById(99L))
                .thenReturn(false);

        boolean result = orderService.deleteOrder(99L);

        assertFalse(result);

        verify(orderRepository).existsById(99L);
        verify(orderRepository, never()).deleteById(99L);
    }
}
