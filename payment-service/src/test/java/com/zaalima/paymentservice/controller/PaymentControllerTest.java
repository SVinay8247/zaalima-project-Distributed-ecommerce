package com.zaalima.paymentservice.controller;

import com.zaalima.paymentservice.entity.Payment;
import com.zaalima.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getAllPayments_shouldReturnPayments() throws Exception {
        Payment payment = new Payment(101L, 500.0, "SUCCESS");

        when(paymentService.getAllPayments())
                .thenReturn(Arrays.asList(payment));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(101))
                .andExpect(jsonPath("$[0].amount").value(500.0))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void getPaymentById_shouldReturnPayment() throws Exception {
        Payment payment = new Payment(101L, 500.0, "SUCCESS");

        when(paymentService.getPaymentById(1L))
                .thenReturn(Optional.of(payment));

        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.amount").value(500.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getPaymentById_shouldReturnNotFound() throws Exception {
        when(paymentService.getPaymentById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_shouldReturnCreatedPayment() throws Exception {
        Payment payment = new Payment(101L, 500.0, "SUCCESS");

        when(paymentService.createPayment(any(Payment.class)))
                .thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": 101,
                                    "amount": 500.0,
                                    "status": "SUCCESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.amount").value(500.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void failPayment_shouldTriggerPaymentFailure() throws Exception {

        mockMvc.perform(post("/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": 10,
                                    "productId": 101,
                                    "quantity": 2
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void updatePayment_shouldReturnUpdatedPayment() throws Exception {
        Payment payment = new Payment(101L, 750.0, "SUCCESS");

        when(paymentService.updatePayment(eq(1L), any(Payment.class)))
                .thenReturn(Optional.of(payment));

        mockMvc.perform(put("/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": 101,
                                    "amount": 750.0,
                                    "status": "SUCCESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.amount").value(750.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void updatePayment_shouldReturnNotFound() throws Exception {
        when(paymentService.updatePayment(eq(99L), any(Payment.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/payments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "orderId": 101,
                                    "amount": 750.0,
                                    "status": "SUCCESS"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePayment_shouldReturnNoContent() throws Exception {
        when(paymentService.deletePayment(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/payments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePayment_shouldReturnNotFound() throws Exception {
        when(paymentService.deletePayment(99L))
                .thenReturn(false);

        mockMvc.perform(delete("/payments/99"))
                .andExpect(status().isNotFound());
    }
}
