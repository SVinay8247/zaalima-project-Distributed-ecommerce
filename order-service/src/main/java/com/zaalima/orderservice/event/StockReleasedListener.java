package com.zaalima.orderservice.event;

import com.zaalima.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReleasedListener {

    private final OrderRepository orderRepository;

    public StockReleasedListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "inventory-events",
            containerFactory = "stockReleasedKafkaListenerContainerFactory"
    )
    public void handleStockReleased(StockReleasedEvent event) {

        if (event.getOrderId() == null
                || event.getStatus() == null) {
            return;
        }

        if ("RELEASED".equalsIgnoreCase(event.getStatus())) {

            orderRepository.findById(event.getOrderId())
                    .ifPresent(order -> {

                        order.setStatus("CANCELLED");
                        orderRepository.save(order);

                        System.out.println(
                                "Order " + event.getOrderId()
                                        + " cancelled after stock release"
                        );
                    });
        }
    }
}
