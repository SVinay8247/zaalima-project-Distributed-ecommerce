package com.zaalima.inventoryservice.listener;

import com.zaalima.inventoryservice.avro.PaymentFailedEvent;
import com.zaalima.inventoryservice.event.StockReleasedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedAvroListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentFailedAvroListener(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "payment-events",
            containerFactory = "paymentFailedAvroKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {

        if (event == null) {
            return;
        }

        if ("FAILED".equalsIgnoreCase(event.getStatus().toString())) {

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
                    "Stock release event published for failed Avro payment, orderId="
                            + event.getOrderId()
            );
        }
    }
}

