package com.zaalima.inventoryservice.listener;

import com.zaalima.inventoryservice.event.PaymentResultEvent;
import com.zaalima.inventoryservice.event.StockReleasedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// @Component
public class PaymentResultListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResultListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "payment-events",
            containerFactory = "paymentResultKafkaListenerContainerFactory",
            groupId = "inventory-payment-group"
    )
    public void handlePaymentResult(PaymentResultEvent event) {

        if (event.getOrderId() == null
                || event.getProductId() == null
                || event.getQuantity() == null
                || event.getStatus() == null) {
            return;
        }

        if ("FAILED".equalsIgnoreCase(event.getStatus())) {

            StockReleasedEvent releasedEvent =
                    new StockReleasedEvent(
                            event.getOrderId(),
                            event.getProductId(),
                            event.getQuantity(),
                            "RELEASED"
                    );

            kafkaTemplate.send(
                    "inventory-events",
                    String.valueOf(event.getOrderId()),
                    releasedEvent
            );

            System.out.println(
                    "Stock release event published for failed payment, orderId="
                            + event.getOrderId()
            );
        }
    }
}
