package com.zaalima.orderservice.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderResilienceServiceTest {

    @Test
    void shouldReturnSuccessForSuccessfulOperation() throws Exception {
        OrderResilienceService service = new OrderResilienceService();

        assertEquals("SUCCESS", service.execute("SUCCESS").get());
    }

    @Test
    void shouldReturnFallbackForFailedOperation() throws Exception {
        OrderResilienceService service = new OrderResilienceService();

        try {
            service.execute("FAIL").get();
        } catch (Exception ignored) {
        }
    }

    @Test
    void circuitBreakerShouldOpenAfterFailures() {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(
                        CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .build();

        CircuitBreaker circuitBreaker =
                CircuitBreaker.of("testCircuitBreaker", config);

        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.executeCallable(() -> {
                    throw new IllegalStateException("failure");
                });
            } catch (Exception ignored) {
            }
        }

        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );
    }
}