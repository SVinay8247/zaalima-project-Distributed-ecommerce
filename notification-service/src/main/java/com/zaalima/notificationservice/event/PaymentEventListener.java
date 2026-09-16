package com.zaalima.notificationservice.event;

import com.zaalima.paymentservice.avro.PaymentSuccessEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-service-group",
            containerFactory = "paymentSuccessKafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(PaymentSuccessEvent event) {

        if (event == null || event.getStatus() == null) {
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(event.getStatus().toString())) {

            System.out.println(
                    "NOTIFICATION: Payment successful for orderId="
                            + event.getOrderId()
                            + ", productId="
                            + event.getProductId()
                            + ", quantity="
                            + event.getQuantity()
            );

        } else if ("FAILED".equalsIgnoreCase(event.getStatus().toString())) {

            System.out.println(
                    "NOTIFICATION: Payment failed for orderId="
                            + event.getOrderId()
                            + ", productId="
                            + event.getProductId()
                            + ", quantity="
                            + event.getQuantity()
            );
        }
    }
}
