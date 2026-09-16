package com.zaalima.orderservice.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderResilienceService {

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    @RateLimiter(name = "orderService")
    @Bulkhead(name = "orderService")
    @TimeLimiter(name = "orderService")
    public CompletableFuture<String> execute(String operation) {

        if ("FAIL".equalsIgnoreCase(operation)) {
            throw new IllegalStateException("Simulated downstream failure");
        }

        return CompletableFuture.completedFuture("SUCCESS");
    }

    private CompletableFuture<String> fallback(
            String operation,
            Throwable throwable) {

        return CompletableFuture.completedFuture("FALLBACK");
    }
}
