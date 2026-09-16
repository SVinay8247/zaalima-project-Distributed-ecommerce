# Notification Service

Notification Service for the Zaalima Distributed E-Commerce project.

## Port

8084

## Purpose

The Notification Service consumes payment-related events through Apache Kafka and provides the event-driven notification component of the application.

## Events

The service works with Avro-based payment events including:

- PaymentSuccessEvent
- PaymentFailedEvent

## Technology

- Java 17
- Spring Boot
- Apache Kafka
- Apache Avro
- Eureka Discovery Client

## Docker

A Dockerfile is provided for containerized deployment.

## Kubernetes

The service has a Kubernetes Deployment and ClusterIP Service defined in:

k8s/notification-service.yaml
