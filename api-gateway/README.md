# API Gateway

Zaalima Distributed E-Commerce API Gateway.

## Port
8090

## Dependencies
- Spring Boot 3.5.15
- Spring Cloud 2025.0.3
- Spring Cloud Gateway Server WebFlux
- Eureka Client
- Config Client

## Expected startup order
1. Service Registry - 8761
2. Config Server - 8888
3. API Gateway - 8090

## Local URL
http://localhost:8090

The gateway uses Eureka service discovery. Once application services are registered,
routes can be reached through the service ID.
