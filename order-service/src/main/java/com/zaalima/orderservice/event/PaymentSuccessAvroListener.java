package com.zaalima.orderservice.event;

import com.zaalima.orderservice.avro.PaymentSuccessEvent;
import com.zaalima.orderservice.entity.Order;
import com.zaalima.orderservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessAvroListener {

    private final OrderRepository orderRepository;

    public PaymentSuccessAvroListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "payment-events",
            containerFactory = "paymentSuccessAvroKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        if (event == null || event.getStatus() == null) {
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(event.getStatus().toString())) {

            orderRepository.findById(event.getOrderId())
                    .ifPresent(order -> {

                        order.setStatus("PAID");
                        orderRepository.save(order);

                        System.out.println(
                                "Order " + event.getOrderId()
                                        + " updated to PAID by Avro PaymentSuccessEvent"
                        );
                    });
        }
    }
}
