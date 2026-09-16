package com.zaalima.orderservice.service;

import com.zaalima.orderservice.avro.OrderCreatedEvent;
import com.zaalima.orderservice.entity.Order;
import com.zaalima.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(
            OrderRepository orderRepository,
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getId() != null) {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getProductId(),
                    savedOrder.getQuantity(),
                    savedOrder.getStatus()
            );

            kafkaTemplate.send(
                    "order-events",
                    String.valueOf(savedOrder.getId()),
                    event
            );
        }

        return savedOrder;
    }

    public Optional<Order> updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    if (updatedOrder.getProductId() != null) {
                        existingOrder.setProductId(updatedOrder.getProductId());
                    }

                    if (updatedOrder.getQuantity() != null) {
                        existingOrder.setQuantity(updatedOrder.getQuantity());
                    }

                    if (updatedOrder.getStatus() != null) {
                        existingOrder.setStatus(updatedOrder.getStatus());
                    }

                    Order savedOrder = orderRepository.save(existingOrder);

                    if (savedOrder.getId() != null) {
                        OrderCreatedEvent event = new OrderCreatedEvent(
                                savedOrder.getId(),
                                savedOrder.getProductId(),
                                savedOrder.getQuantity(),
                                savedOrder.getStatus()
                        );

                        kafkaTemplate.send(
                                "order-events",
                                String.valueOf(savedOrder.getId()),
                                event
                        );
                    }

                    return savedOrder;
                });
    }

    public boolean deleteOrder(Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }

        return false;
    }
}
