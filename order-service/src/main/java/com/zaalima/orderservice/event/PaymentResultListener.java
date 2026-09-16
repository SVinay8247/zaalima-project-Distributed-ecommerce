package com.zaalima.orderservice.event;

import com.zaalima.orderservice.entity.Order;
import com.zaalima.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// @Component
public class PaymentResultListener {

    private final OrderRepository orderRepository;

    public PaymentResultListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-payment-group"
    )
    public void handlePaymentResult(PaymentResultEvent event) {

        if (event.getOrderId() == null || event.getStatus() == null) {
            return;
        }

        orderRepository.findById(event.getOrderId())
                .ifPresent(order -> {

                    if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                        order.setStatus("PAID");
                    } else {
                        order.setStatus("PAYMENT_FAILED");
                    }

                    orderRepository.save(order);

                    System.out.println(
                            "Order " + event.getOrderId()
                                    + " updated to " + order.getStatus()
                    );
                });
    }
}
