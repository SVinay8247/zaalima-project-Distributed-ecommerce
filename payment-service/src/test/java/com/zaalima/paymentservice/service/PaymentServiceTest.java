package com.zaalima.paymentservice.service;

import com.zaalima.paymentservice.dto.PaymentFailureRequest;
import com.zaalima.paymentservice.entity.Payment;
import com.zaalima.paymentservice.avro.PaymentFailedEvent;
import com.zaalima.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, org.apache.avro.specific.SpecificRecord> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getAllPayments_shouldReturnPayments() {
        Payment payment = new Payment(1L, 499.99, "PENDING");
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
        assertEquals(499.99, result.get(0).getAmount());
        verify(paymentRepository).findAll();
    }

    @Test
    void getPaymentById_shouldReturnPaymentWhenFound() {
        Payment payment = new Payment(1L, 499.99, "PENDING");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getPaymentById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getOrderId());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPaymentById_shouldReturnEmptyWhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.getPaymentById(99L);

        assertTrue(result.isEmpty());
        verify(paymentRepository).findById(99L);
    }

    @Test
    void createPayment_shouldSavePayment() {
        Payment payment = new Payment(1L, 499.99, "PENDING");
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = paymentService.createPayment(payment);

        assertSame(payment, result);
        verify(paymentRepository).save(payment);
    }

    @Test
    void updatePayment_shouldUpdateWhenFound() {
        Payment existing = new Payment(1L, 499.99, "PENDING");
        Payment updated = new Payment(1L, 599.99, "COMPLETED");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(existing);

        Optional<Payment> result = paymentService.updatePayment(1L, updated);

        assertTrue(result.isPresent());
        assertEquals(599.99, result.get().getAmount());
        assertEquals("COMPLETED", result.get().getStatus());
        verify(paymentRepository).save(existing);
    }

    @Test
    void updatePayment_shouldReturnEmptyWhenNotFound() {
        Payment updated = new Payment(1L, 599.99, "COMPLETED");
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.updatePayment(99L, updated);

        assertTrue(result.isEmpty());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void deletePayment_shouldReturnTrueWhenPaymentExists() {
        when(paymentRepository.existsById(1L)).thenReturn(true);

        boolean result = paymentService.deletePayment(1L);

        assertTrue(result);
        verify(paymentRepository).deleteById(1L);
    }

    @Test
    void deletePayment_shouldReturnFalseWhenPaymentDoesNotExist() {
        when(paymentRepository.existsById(99L)).thenReturn(false);

        boolean result = paymentService.deletePayment(99L);

        assertFalse(result);
        verify(paymentRepository, never()).deleteById(anyLong());
    }

    @Test
    void publishPaymentFailure_shouldSaveFailedPaymentAndPublishEvent() {

        PaymentFailureRequest request = new PaymentFailureRequest();
        request.setOrderId(10L);
        request.setProductId(101L);
        request.setQuantity(2);

        Payment savedPayment = new Payment(10L, 0.0, "FAILED");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        paymentService.publishPaymentFailure(request);

        verify(paymentRepository).save(any(Payment.class));

        verify(kafkaTemplate).send(
                eq("payment-events"),
                eq("10"),
                argThat((PaymentFailedEvent event) ->
                        event.getOrderId() == 10L
                                && event.getProductId() == 101L
                                && event.getQuantity() == 2
                                && "FAILED".equals(event.getStatus())
                )
        );
    }
}


