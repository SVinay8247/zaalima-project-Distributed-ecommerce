# Zaalima Project 2 - Distributed E-Commerce Microservices

A distributed e-commerce application built using a microservices architecture with event-driven communication, Apache Kafka, Apache Avro, Choreography Saga, Resilience4j, Micrometer/Prometheus monitoring, distributed tracing with Zipkin, Docker, Kubernetes, and a responsive web frontend.

The project demonstrates a complete distributed order lifecycle, including successful payment processing and payment-failure compensation with inventory restoration.

---

## Project Overview

This project implements an event-driven distributed e-commerce system using independently deployable Spring Boot microservices.

The system uses:

- Eureka for service discovery
- Spring Cloud Config for centralized configuration
- Spring Cloud Gateway as the API entry point
- Apache Kafka for asynchronous event-driven communication
- Apache Avro for event serialization
- Choreography Saga for distributed transaction management
- Resilience4j for fault tolerance
- Micrometer and Prometheus for application metrics
- Micrometer Tracing and Zipkin for distributed tracing
- PostgreSQL for persistent data
- Docker for containerization
- Kubernetes for deployment
- JWT for API Gateway authentication
- HTML, CSS, JavaScript and Bootstrap for the frontend

---

## Architecture

The application follows a distributed microservices architecture in which each service has a focused responsibility and can be developed, deployed, and scaled independently.

### Microservices

| Service | Port | Responsibility |
|---|---:|---|
| Service Registry | 8761 | Eureka-based service discovery |
| Config Server | 8888 | Centralized application configuration |
| API Gateway | 8090 | Client entry point, routing, and JWT authentication |
| Order Service | 8081 | Order creation and order state management |
| Inventory Service | 8082 | Inventory lookup and stock reservation/release |
| Payment Service | 8083 | Payment processing and payment-failure handling |
| Notification Service | 8084 | Processes notification-related backend events |

### Infrastructure

| Component | Purpose |
|---|---|
| PostgreSQL | Persistent application data |
| Apache Kafka | Asynchronous event communication |
| Apache Avro | Business event serialization |
| Eureka | Service discovery |
| Spring Cloud Config | Centralized configuration |
| Prometheus | Metrics collection and monitoring |
| Zipkin | Distributed tracing |
| Docker | Containerization |
| Kubernetes | Container orchestration |

### High-Level Architecture

    Client / Frontend / Postman
                |
                | REST + JWT
                v
          API Gateway
                |
                v
        Eureka Service Registry
                |
       +--------+--------+----------------+
       |                 |                |
       v                 v                v
    Order            Inventory          Payment
    Service           Service           Service
       |                 |                |
       +-----------------+----------------+
                         |
                         v
                       Kafka
                         |
                         v
                  Notification
                     Service

The API Gateway provides the main entry point for external clients.

Eureka provides service discovery so that services can locate one another dynamically.

Kafka is used for asynchronous communication between participating microservices.

PostgreSQL provides persistent storage for the application data.

Prometheus and Micrometer provide application metrics, while Micrometer Tracing and Zipkin provide distributed tracing.

Docker packages the services into containers and Kubernetes provides the deployment environment for the distributed application.

---
## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Apache Kafka
- Apache Avro
- Resilience4j
- Micrometer
- Micrometer Tracing
- Brave
- Zipkin Reporter
- PostgreSQL
- Maven

### Frontend

- HTML5
- CSS3
- JavaScript ES6+
- Bootstrap 5
- Bootstrap Icons
- Fetch API
- Browser Local Storage
- Browser Session Storage

### DevOps and Infrastructure

- Docker
- Kubernetes
- kubectl
- Prometheus
- Zipkin
- Git
- GitHub

---
## Event-Driven Communication

Apache Kafka is used for asynchronous communication between the microservices.

Apache Avro is used for serialization of the main business events exchanged through Kafka.

The client communicates with the API Gateway using REST APIs, while backend microservices communicate asynchronously through Kafka events.

### Communication Flow

    Frontend
       |
       | REST + JWT
       v
    API Gateway
       |
       v
    Order Service
       |
       | Order Created Event
       v
    Inventory Service
       |
       | Stock Reserved Event
       v
    Payment Service
       |
       | Payment Success Event
       v
    Order Service
       |
       v
    Order Status = PAID

### Main Business Events

| Event | Producer | Consumer | Purpose |
|---|---|---|---|
| Order Created Event | Order Service | Inventory Service | Requests inventory reservation |
| Stock Reserved Event | Inventory Service | Payment Service | Allows payment processing |
| Payment Success Event | Payment Service | Order Service | Updates order to `PAID` |
| Payment Failed Event | Payment Service | Inventory Service | Triggers inventory compensation |
| Stock Released Event | Inventory Service | Order Service | Completes compensation and cancels the order |

### Event-Driven Workflow

    Order Service
         |
         | Order Created Event
         v
    Inventory Service
         |
         | Stock Reserved Event
         v
    Payment Service
         |
         +------------------------+
         |                        |
         | Payment Success        | Payment Failure
         v                        v
    Order Service          Payment Failed Event
         |                        |
         |                        v
         |                Inventory Service
         |                        |
         |                        | Release Stock
         |                        v
         |                Stock Released Event
         |                        |
         +------------------------+
                  |
                  v
            Order Service
                  |
           +------+------+
           |             |
           v             v
         PAID        CANCELLED

Kafka provides asynchronous communication between the participating services, while Apache Avro provides a structured serialization format for business events.

The frontend does not communicate directly with Kafka. All browser requests are sent through the API Gateway using REST APIs.

---
## Choreography Saga

The project uses a choreography-based Saga pattern for managing the distributed order workflow.

Each participating microservice reacts to events published by another service. There is no central Saga orchestrator controlling the complete transaction.

### Successful Saga Flow

    Order Created
          |
          v
    Inventory Service
          |
          | Reserve Stock
          v
    Stock Reserved Event
          |
          v
    Payment Service
          |
          | Process Payment
          v
    Payment Success Event
          |
          v
    Order Service
          |
          v
    Order Status = PAID

### Payment Failure Compensation Flow

    Order Created
          |
          v
    Inventory Service
          |
          | Reserve Stock
          v
    Stock Reserved Event
          |
          v
    Payment Service
          |
          | Payment Failure
          v
    Payment Failed Event
          |
          v
    Inventory Service
          |
          | Release Reserved Stock
          v
    Stock Released Event
          |
          v
    Order Service
          |
          v
    Order Status = CANCELLED

### Compensation

When payment fails after inventory has already been reserved, the Payment Service publishes a Payment Failed Event.

The Inventory Service consumes the event and releases the previously reserved quantity.

After inventory is released, the Stock Released Event is published and the Order Service updates the order status to `CANCELLED`.

This compensation mechanism prevents reserved inventory from remaining unavailable after a failed payment.

---
## Resilience and Fault Tolerance

Resilience4j is used to improve the reliability of the distributed services and handle temporary failures between service interactions.

### Resilience4j Features

The project uses the following resilience mechanisms:

- Circuit Breaker
- Retry
- Time Limiter
- Fallback

### Circuit Breaker

The Circuit Breaker helps prevent repeated calls to an unhealthy dependency.

When failures exceed the configured threshold, the circuit can open and temporarily stop further calls to the failing dependency.

This helps reduce cascading failures across the distributed system.

### Retry

Retry mechanisms allow failed operations to be attempted again when the failure may be temporary.

This is useful for handling transient communication or service failures.

### Time Limiter

The Time Limiter helps prevent operations from waiting indefinitely for a response.

This allows the application to fail within a controlled time period when a dependent operation does not respond as expected.

### Fallback

Fallback handling provides an alternative response or controlled failure path when a protected operation cannot be completed successfully.

These resilience mechanisms are especially useful in a distributed architecture where individual services and infrastructure components can fail independently.

---
## Monitoring and Observability

The project uses Micrometer, Spring Boot Actuator, and Prometheus to provide application-level metrics and monitoring.

### Micrometer

Micrometer provides a common metrics instrumentation layer for the Spring Boot services.

It allows application and runtime metrics to be exposed in a format that can be collected by monitoring systems.

### Spring Boot Actuator

Spring Boot Actuator provides operational endpoints for monitoring application health and runtime information.

The API Gateway exposes the health endpoint used during deployment and verification.

Example:

    http://localhost:8090/actuator/health

### Prometheus

Prometheus is used to collect and monitor application metrics exposed by the services.

Default local Prometheus URL:

    http://localhost:9090

### Observability Flow

    Spring Boot Services
            |
            v
       Micrometer
            |
            v
    Actuator Metrics
            |
            v
        Prometheus
            |
            v
       Monitoring

Monitoring is useful for observing service health, application behavior, and runtime metrics in the distributed environment.

---
## Distributed Tracing

The project uses Micrometer Tracing with Brave and Zipkin Reporter for distributed tracing.

Distributed tracing helps track a request or business operation as it moves across multiple microservices.

### Tracing Flow

    Client
      |
      v
    API Gateway
      |
      v
    Order Service
      |
      v
    Inventory Service
      |
      v
    Payment Service
      |
      v
    Order Service

Tracing information can be propagated across service boundaries so that distributed operations can be observed as a single trace.

### Zipkin

Zipkin is used as the distributed tracing backend.

Default local Zipkin endpoint:

    http://localhost:9411

Zipkin API endpoint:

    http://localhost:9411/api/v2/spans

The Zipkin runtime is optional during normal application execution and depends on a running Zipkin instance.

### Benefits

Distributed tracing helps with:

- Following requests across multiple services
- Identifying slow operations
- Investigating failures
- Understanding service-to-service communication
- Debugging distributed workflows

Together, Micrometer Tracing and Zipkin provide visibility into the distributed request flow.

---
## Security and JWT Authentication

The API Gateway uses JWT-based authentication through Spring Security OAuth2 Resource Server.

External clients such as Postman and the frontend send a JWT access token with protected API requests.

### Authentication Flow

    Client
       |
       | Authorization: Bearer <JWT>
       v
    API Gateway
       |
       | JWT Validation
       v
    Protected API Route
       |
       v
    Microservice

Requests without a valid JWT token are rejected by the API Gateway.

### JWT Configuration

The JWT signing secret is supplied through external configuration using the `JWT_SECRET` environment variable.

The secret is not stored directly in the source code or committed to Git.

For Kubernetes, the JWT secret is supplied through a Kubernetes Secret.

### Local JWT Token Generation

For local development and API demonstrations, a valid HS256 JWT can be generated using the configured JWT secret.

PowerShell example:

    $JWT_SECRET = kubectl get secret jwt-secret -o jsonpath="{.data.JWT_SECRET}" | ForEach-Object {
        [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_))
    }

The secret should be kept private and should never be printed, committed, or shared publicly.

A JWT can then be generated locally with an appropriate `iat` and `exp` claim and signed using HMAC-SHA256.

### Using the JWT Token

For Postman:

    Authorization
    Type: Bearer Token
    Token: <generated JWT token>

For the frontend, the access token is stored in browser session storage under:

    zaalima_access_token

The frontend API layer automatically adds the token to authenticated requests using the HTTP Authorization header.

### Authentication Verification

The protected API can be verified by making the same request with and without a JWT.

Without a token:

    HTTP 401 Unauthorized

With a valid JWT:

    HTTP 200 OK

This confirms that authentication is enforced at the API Gateway before requests reach the protected backend routes.

---
## API Gateway and REST API

The API Gateway acts as the single entry point for external clients such as the frontend application and Postman.

It is responsible for receiving HTTP requests, validating JWT authentication, and routing requests to the appropriate backend microservice.

### Gateway Port

    http://localhost:8090

When using Kubernetes with local port forwarding:

    http://localhost:18090

### Gateway Routes

| Route | Target Service | Purpose |
|---|---|---|
| `/orders/**` | ORDER-SERVICE | Order creation and order queries |
| `/inventory/**` | INVENTORY-SERVICE | Inventory lookup |
| `/payments/**` | PAYMENT-SERVICE | Payment-related operations |

The Gateway uses Eureka service discovery and load-balanced routes to locate backend services.

### REST API Endpoints

#### Inventory

    GET /inventory

Returns the available inventory records.

    GET /inventory/{id}

Returns a specific inventory record by its ID.

#### Orders

    POST /orders

Creates a new order using the product ID and requested quantity.

Example request body:

    {
        "productId": 101,
        "quantity": 1
    }

    GET /orders

Returns the available orders.

    GET /orders/{id}

Returns a specific order by its ID.

#### Payment Failure Testing

    POST /payments/fail

This endpoint is available for demonstrating the payment-failure compensation workflow.

Example request body:

    {
        "orderId": 56,
        "productId": 101,
        "quantity": 1
    }

The endpoint triggers the payment-failure event used to demonstrate inventory compensation and order cancellation.

### Frontend to Backend Communication

The frontend communicates with the backend only through the API Gateway.

The frontend API layer is implemented in:

    frontend/js/api.js

The configured frontend API base URL is:

    http://localhost:18090

Example request flow:

    product-details.html
          |
          v
    product-details.js
          |
          v
    api.js
          |
          | GET /inventory/1
          v
    API Gateway
          |
          v
    Inventory Service
          |
          v
    JSON Response
          |
          v
    Frontend UI

The frontend does not communicate directly with Kafka, Eureka, PostgreSQL, or individual backend service ports.

---
## Frontend

The project includes a responsive web frontend for interacting with the e-commerce backend through the API Gateway.

The frontend is implemented using standard web technologies without a frontend framework.

### Frontend Technology

- HTML5
- CSS3
- Vanilla JavaScript ES6+
- Bootstrap 5
- Bootstrap Icons
- Fetch API
- Local Storage
- Session Storage

### Frontend Pages

| Page | Purpose |
|---|---|
| `index.html` | Home page |
| `products.html` | Product and inventory listing |
| `product-details.html` | Product details and stock information |
| `cart.html` | Shopping cart |
| `checkout.html` | Order checkout |
| `orders.html` | Customer order listing |
| `order-details.html` | Individual order details and status |
| `profile.html` | Profile area and authentication state |
| `notifications.html` | Notification availability state |

### Frontend JavaScript Modules

The frontend separates API communication and page-specific behavior into modular JavaScript files.

Important modules include:

    frontend/js/api.js
    frontend/js/auth.js
    frontend/js/app.js
    frontend/js/cart-state.js
    frontend/js/cart.js
    frontend/js/checkout.js
    frontend/js/orders.js
    frontend/js/order-details.js
    frontend/js/products.js
    frontend/js/product-details.js
    frontend/js/profile.js
    frontend/js/notifications.js

### API Integration

The central API layer is implemented in:

    frontend/js/api.js

The frontend uses the API Gateway as its backend base URL:

    http://localhost:18090

The main API operations used by the frontend are:

| Frontend Operation | HTTP Method | Gateway Endpoint |
|---|---|---|
| Load inventory | GET | `/inventory` |
| Load inventory item | GET | `/inventory/{id}` |
| Create order | POST | `/orders` |
| Load orders | GET | `/orders` |
| Load order details | GET | `/orders/{id}` |

### Authentication

Authenticated API requests include the JWT access token through the HTTP Authorization header.

The frontend stores the access token in browser session storage using:

    zaalima_access_token

The authentication module is implemented in:

    frontend/js/auth.js

The frontend does not store the JWT secret.

### Shopping Cart

The shopping cart is maintained on the client side using browser Local Storage.

Cart data is stored using the key:

    zaalima-cart

The cart state is shared across the relevant frontend pages.

### Order Flow

The frontend supports the following user flow:

    Home
      |
      v
    Products
      |
      v
    Product Details
      |
      v
    Add to Cart
      |
      v
    Cart
      |
      v
    Checkout
      |
      v
    Create Order
      |
      v
    Order Status
      |
      v
    My Orders / Order Details

The frontend is responsible for presentation, user interaction, cart state, and REST API communication.

The distributed order processing, Kafka events, Saga workflow, payment processing, inventory reservation, compensation, and persistent backend state remain responsibilities of the backend microservices.

### Frontend and Backend Boundary

The frontend does not directly access:

- Kafka
- Eureka
- PostgreSQL
- Avro event streams
- Individual microservice ports

All browser API requests are sent through the API Gateway.

This keeps the frontend independent from the internal microservice communication architecture.

---
## Docker and Containerization

Docker is used to package the microservices and supporting application components into portable containers.

Each backend service that requires containerization has its own Dockerfile.

### Dockerized Services

Dockerfiles are provided for:

- API Gateway
- Config Server
- Service Registry
- Order Service
- Inventory Service
- Payment Service
- Notification Service

The services use Java 17 runtime containers.

### Containerization Flow

    Source Code
        |
        v
    Maven Build
        |
        v
    JAR Artifact
        |
        v
    Docker Image
        |
        v
    Container
        |
        v
    Kubernetes Deployment

### Docker Images

The project uses Docker images for deploying the backend services into the Kubernetes environment.

Example image naming pattern:

    zaalima/order-service
    zaalima/inventory-service
    zaalima/payment-service
    zaalima/notification-service
    zaalima/api-gateway

Images can be tagged with version-specific tags during development and deployment.

### Docker Build Example

From the project root, a service image can be built after packaging the service:

    docker build -t zaalima/order-service:<tag> ./order-service

The same approach can be used for the other Dockerized services.

### Kubernetes Integration

The Docker images are used as container images in the Kubernetes Deployment manifests under:

    k8s/

Kubernetes manages the resulting containers as Pods and provides the service networking required by the distributed application.

Docker therefore provides the application packaging layer, while Kubernetes provides container orchestration and deployment management.

---
## Kubernetes Deployment

Kubernetes is used to deploy and manage the containerized microservices in a distributed environment.

The Kubernetes manifests are maintained under:

    k8s/

### Kubernetes Components

The deployment includes:

- Service Registry
- Config Server
- Apache Kafka
- Order Service
- Inventory Service
- Payment Service
- Notification Service
- API Gateway

### Kubernetes Manifests

The main deployment manifests include:

    service-registry.yaml
    config-server.yaml
    kafka.yaml
    order-service.yaml
    inventory-service.yaml
    payment-service.yaml
    notification-service.yaml
    api-gateway.yaml

### Kubernetes Architecture

    Kubernetes Cluster
           |
           +----------------------+
           |                      |
           v                      v
    Infrastructure          Application Services
           |                      |
     +-----+------+       +-------+--------+
     |            |       |       |        |
    Kafka       Eureka   Order  Inventory Payment
     |            |       |       |        |
     +------------+-------+-------+--------+
                         |
                         v
                    API Gateway
                         |
                         v
                  External Clients

### Service Discovery in Kubernetes

Eureka is used for service discovery between the microservices.

Kubernetes service names are used as stable network identities for service-to-service communication.

For example:

    inventory-service
    order-service
    payment-service
    notification-service
    kafka

The Eureka configuration is adjusted so that registered service hostnames resolve correctly within the Kubernetes environment.

### Configuration and Secrets

Application configuration is managed through the Config Server.

Sensitive configuration such as the JWT secret and database credentials is supplied externally through Kubernetes Secrets and environment variables.

Secrets are not committed to the repository.

### Useful Kubernetes Commands

Check running Pods:

    kubectl get pods

Check Services:

    kubectl get services

Check Deployments:

    kubectl get deployments

Check the status of a Deployment:

    kubectl rollout status deployment/<deployment-name>

View service logs:

    kubectl logs deployment/<deployment-name>

### API Gateway Port Forwarding

For local browser or Postman access to the Kubernetes API Gateway:

    kubectl port-forward service/api-gateway 18090:8090

The Gateway can then be accessed through:

    http://localhost:18090

### Kubernetes Verification

The Kubernetes environment was used to verify the distributed order workflow.

The successful workflow was verified as:

    Order Created
        |
        v
    Inventory Reserved
        |
        v
    Payment Successful
        |
        v
    Order = PAID

The payment-failure compensation workflow was also verified:

    Order Created
        |
        v
    Inventory Reserved
        |
        v
    Payment Failed
        |
        v
    Stock Released
        |
        v
    Order = CANCELLED

Inventory restoration was verified after the compensation workflow.

---
## Running the Project

The project can be run locally for development and testing, or deployed to Kubernetes for distributed environment verification.

### Prerequisites

Make sure the following tools and infrastructure are available:

- Java 17
- Maven Wrapper
- Docker
- Kubernetes
- kubectl
- PostgreSQL
- Apache Kafka

For monitoring and tracing, Prometheus and Zipkin can also be started when required.

### Local Service Startup Order

A recommended startup order is:

    1. PostgreSQL
    2. Apache Kafka
    3. Service Registry
    4. Config Server
    5. Order Service
    6. Inventory Service
    7. Payment Service
    8. Notification Service
    9. API Gateway

The infrastructure and supporting services should be available before starting services that depend on them.

### Build the Project

From the project root:

    .\mvnw.cmd clean package

To run tests during the Maven build:

    .\mvnw.cmd clean test

Individual services can also be built from their respective directories.

Example:

    cd .\order-service
    .\mvnw.cmd clean package
    cd ..

### Running the API Gateway Locally

The API Gateway runs on:

    http://localhost:8090

The Gateway requires the JWT secret to be supplied through external configuration.

Do not place the JWT secret directly in the source code or commit it to the repository.

### Running with Kubernetes

Check the Kubernetes environment:

    kubectl get pods

    kubectl get services

    kubectl get deployments

Once the required Pods and Services are running, expose the API Gateway locally using port forwarding:

    kubectl port-forward service/api-gateway 18090:8090

The Kubernetes API Gateway can then be accessed through:

    http://localhost:18090

### Frontend Development

The frontend can be served using a local development server such as VS Code Live Server.

The frontend should be opened through:

    http://localhost:5500

The frontend communicates with the Kubernetes API Gateway through:

    http://localhost:18090

The configured CORS origin for the frontend is:

    http://localhost:5500

Use `localhost` consistently when accessing the frontend. Using `127.0.0.1:5500` is not equivalent to the configured CORS origin.

### JWT for Local Demonstration

Protected Gateway APIs require a valid JWT access token.

For local demonstrations, generate a valid JWT using the externally supplied JWT secret and use it as a Bearer token in Postman or the frontend session storage.

The actual secret and generated token must not be committed to GitHub or included in this README.

### Basic Verification

After starting the required services, verify the environment in this order:

    1. Check Kubernetes Pods
    2. Check API Gateway health
    3. Start frontend
    4. Ensure a valid JWT is available
    5. Open Products
    6. Verify inventory data loads
    7. Create a test order
    8. Verify the resulting order status
    9. Verify the Saga workflow through backend logs or API queries

API Gateway health endpoint:

    http://localhost:18090/actuator/health

A healthy Gateway should return HTTP 200.

---
## End-to-End Testing and Demo Workflow

The distributed order workflow was verified end-to-end through the API Gateway using authenticated API requests.

The verification covered both the successful payment workflow and the payment-failure compensation workflow.

### Authentication Verification

Before testing protected APIs, the API Gateway security was verified.

Request without JWT:

    GET /inventory

Expected result:

    HTTP 401 Unauthorized

Request with a valid JWT:

    GET /inventory

Expected result:

    HTTP 200 OK

This confirms that protected API access is enforced at the Gateway.

### Successful Order Workflow

A successful order workflow was verified using the following sequence.

#### Step 1 - Check Inventory

    GET /inventory/1

The inventory record was retrieved successfully.

#### Step 2 - Create Order

    POST /orders

Example request:

    {
        "productId": 101,
        "quantity": 1
    }

The API returned a newly created order with an initial status of:

    CREATED

#### Step 3 - Inventory Reservation

The Order Created Event was processed asynchronously by the Inventory Service.

The requested quantity was reserved and the Stock Reserved Event was published.

#### Step 4 - Payment Processing

The Payment Service consumed the Stock Reserved Event and processed the payment.

A successful payment resulted in a Payment Success Event.

#### Step 5 - Verify Order

    GET /orders/{orderId}

The order status was verified as:

    PAID

### Payment Failure and Compensation Workflow

The compensation workflow was verified separately.

#### Step 1 - Create Failure-Test Order

    POST /orders

Example request:

    {
        "productId": 101,
        "quantity": 1
    }

The newly created order initially had:

    CREATED

#### Step 2 - Trigger Payment Failure

    POST /payments/fail

Example request:

    {
        "orderId": 56,
        "productId": 101,
        "quantity": 1
    }

This endpoint is used to trigger the payment-failure event for demonstrating the compensation workflow.

#### Step 3 - Inventory Compensation

The Payment Failed Event was consumed by the Inventory Service.

The previously reserved inventory quantity was released.

The Inventory Service then published the Stock Released Event.

#### Step 4 - Verify Cancelled Order

    GET /orders/56

The order status was verified as:

    CANCELLED

#### Step 5 - Verify Inventory Restoration

    GET /inventory/1

The inventory quantity was checked before and after the compensation workflow.

The quantity returned to its previous value after the reserved stock was released.

This confirms that the compensation workflow restored the inventory after payment failure.

### Verified Distributed Workflow

Successful workflow:

    POST /orders
          |
          v
    Order = CREATED
          |
          v
    Inventory Reserved
          |
          v
    Payment Successful
          |
          v
    Order = PAID

Failure workflow:

    POST /orders
          |
          v
    Order = CREATED
          |
          v
    Inventory Reserved
          |
          v
    Payment Failed
          |
          v
    Inventory Released
          |
          v
    Order = CANCELLED

### End-to-End Result

The Kubernetes environment successfully demonstrated:

- JWT-protected API access
- Inventory retrieval
- Order creation
- Asynchronous Kafka event processing
- Inventory reservation
- Successful payment processing
- Order transition to `PAID`
- Payment failure handling
- Inventory compensation
- Order transition to `CANCELLED`
- Inventory restoration

---
## Project Structure

The repository is organized into separate directories for each backend service, shared configuration, Kubernetes deployment, and frontend application.

### Root Structure

    zaalima-project-2-distributed-ecommerce/
    |
    +-- api-gateway/
    |
    +-- config-server/
    |
    +-- service-registry/
    |
    +-- order-service/
    |
    +-- inventory-service/
    |
    +-- payment-service/
    |
    +-- notification-service/
    |
    +-- config-repository/
    |
    +-- frontend/
    |
    +-- k8s/
    |
    +-- README.md

### Backend Services

#### API Gateway

    api-gateway/

Provides the external API entry point, JWT authentication, CORS configuration, and routing to backend services.

#### Config Server

    config-server/

Provides centralized configuration for the distributed services.

#### Service Registry

    service-registry/

Provides Eureka-based service discovery.

#### Order Service

    order-service/

Responsible for order creation, order persistence, and order status management.

#### Inventory Service

    inventory-service/

Responsible for inventory lookup, stock reservation, and stock release during compensation.

#### Payment Service

    payment-service/

Responsible for payment processing and payment-failure event handling.

#### Notification Service

    notification-service/

Processes notification-related backend events.

### Configuration Repository

    config-repository/

Contains centralized configuration used by the Config Server.

### Kubernetes

    k8s/

Contains Kubernetes manifests used to deploy the distributed application.

### Frontend

    frontend/
    |
    +-- index.html
    +-- products.html
    +-- product-details.html
    +-- cart.html
    +-- checkout.html
    +-- orders.html
    +-- order-details.html
    +-- profile.html
    +-- notifications.html
    |
    +-- css/
    |   +-- style.css
    |
    +-- js/
        +-- api.js
        +-- app.js
        +-- auth.js
        +-- cart-state.js
        +-- cart.js
        +-- checkout.js
        +-- notifications.js
        +-- order-details.js
        +-- orders.js
        +-- product-details.js
        +-- products.js
        +-- profile.js

The frontend is kept separate from the backend microservices and communicates with them through the API Gateway.

---
## Testing and Quality

The project includes service-level tests for the core backend microservices and end-to-end verification of the distributed order workflows.

### Service-Level Tests

Tests are available for:

- Order Service
- Inventory Service
- Payment Service
- Notification Service

The tests help verify service-specific behavior and event-processing logic.

### Maven Test Execution

Tests can be executed from an individual service directory using:

    .\mvnw.cmd clean test

A complete build without running tests can be performed using:

    .\mvnw.cmd clean package -DskipTests

### Distributed Workflow Verification

In addition to service-level tests, the distributed workflows were verified through the API Gateway.

The successful workflow verified:

    Order Created
        |
        v
    Inventory Reserved
        |
        v
    Payment Successful
        |
        v
    Order = PAID

The failure workflow verified:

    Order Created
        |
        v
    Inventory Reserved
        |
        v
    Payment Failed
        |
        v
    Inventory Released
        |
        v
    Order = CANCELLED

### Security Testing

The API Gateway authentication behavior was also verified.

Unauthenticated protected request:

    HTTP 401 Unauthorized

Authenticated request with a valid JWT:

    HTTP 200 OK

### Kubernetes Verification

The distributed application was tested in Kubernetes with the services communicating through their Kubernetes networking and service discovery configuration.

The verification included:

- Pod availability
- API Gateway health
- Eureka service registration
- Kafka connectivity
- Authenticated API access
- Inventory operations
- Order creation
- Successful payment workflow
- Payment-failure compensation
- Inventory restoration

### Testing Approach

The project uses a combination of:

- Unit/service-level testing
- API-level verification
- End-to-end workflow testing
- Kubernetes deployment verification
- Authentication verification

This combination validates both individual service behavior and the complete distributed business workflow.

---
## Postman and API Demo

Postman can be used to demonstrate and verify the REST APIs exposed through the API Gateway.

The API Gateway must be running and accessible before starting the API demo.

For Kubernetes-based testing with local port forwarding:

    http://localhost:18090

### Authentication

Protected APIs require a valid JWT access token.

In Postman, configure:

    Authorization
    Type: Bearer Token
    Token: <generated JWT token>

Do not store or commit the JWT secret or generated token in the repository.

### Recommended Demo Sequence

A clean demonstration can be performed in the following order.

#### 1. Verify Unauthorized Access

    GET /inventory

Send the request without authentication.

Expected result:

    HTTP 401 Unauthorized

This demonstrates that the API Gateway protects the backend APIs.

#### 2. Get Inventory

    GET /inventory

Send the request with a valid Bearer token.

Expected result:

    HTTP 200 OK

#### 3. Get Inventory by ID

    GET /inventory/1

Expected result:

    HTTP 200 OK

The response contains the inventory record for the requested ID.

#### 4. Create a Successful Order

    POST /orders

Request body:

    {
        "productId": 101,
        "quantity": 1
    }

The initial response contains an order with status:

    CREATED

The backend then processes the distributed Saga asynchronously.

#### 5. Verify Successful Order

    GET /orders/{orderId}

After the Saga completes, the order should show:

    PAID

#### 6. Create a Failure-Test Order

    POST /orders

Request body:

    {
        "productId": 101,
        "quantity": 1
    }

Record the returned order ID.

#### 7. Trigger Payment Failure

    POST /payments/fail

Request body:

    {
        "orderId": <orderId>,
        "productId": 101,
        "quantity": 1
    }

The endpoint triggers the payment-failure event used for the compensation demonstration.

#### 8. Verify Compensation

    GET /orders/{orderId}

Expected final status:

    CANCELLED

Then verify inventory:

    GET /inventory/1

The previously reserved quantity should have been released.

### Demo Flow Summary

    Authentication
         |
         v
    Inventory
         |
         v
    Create Order
         |
         v
    Order = CREATED
         |
         v
    Kafka Saga Processing
         |
         +------------------+
         |                  |
         v                  v
    Payment Success     Payment Failure
         |                  |
         v                  v
    Order = PAID       Stock Released
                            |
                            v
                      Order = CANCELLED

This sequence provides a simple demonstration of API Gateway security, REST API integration, Kafka-based event processing, successful Saga execution, and compensation handling.

---
## Repository Safety and Configuration

The repository follows an external-configuration approach for sensitive values and environment-specific settings.

Sensitive credentials should never be hardcoded into application source code or committed to Git.

### Sensitive Configuration

The following types of values should be supplied externally:

- JWT signing secret
- Database credentials
- Environment-specific infrastructure configuration

### JWT Secret

The API Gateway receives the JWT signing secret through the `JWT_SECRET` environment variable.

For Kubernetes, the secret is supplied through a Kubernetes Secret.

The actual secret value is intentionally not included in this README.

### Database Credentials

Database credentials are supplied through external configuration and Kubernetes Secrets where required.

Credentials should not be stored directly in source files or committed to the repository.

### Git Ignore

The repository uses `.gitignore` to exclude development-specific and generated files such as:

    .vscode/
    target/
    *.log
    .maven/

Build output and local development files should remain outside the committed source.

### Secret Handling Rules

Before pushing changes to GitHub:

- Do not commit JWT secrets.
- Do not commit database passwords.
- Do not commit generated access tokens.
- Do not commit local environment secrets.
- Review changed files before creating a commit.
- Keep sensitive configuration external to the repository.

### Configuration Principle

The project follows this general configuration flow:

    Application Code
          |
          v
    External Configuration
          |
          +------------------+
          |                  |
          v                  v
    Environment        Kubernetes Secret
      Variables
          |
          v
    Running Service

This keeps application source code separate from sensitive environment-specific configuration.

---
## Project Highlights

The project demonstrates several important concepts used in modern distributed backend systems.

### Microservices Architecture

The application is divided into independently deployable services with clearly separated responsibilities.

### Service Discovery

Eureka enables dynamic service discovery between the distributed services.

### Centralized Configuration

Spring Cloud Config provides centralized configuration management.

### API Gateway

Spring Cloud Gateway provides a single entry point for external clients and handles routing and JWT authentication.

### Event-Driven Architecture

Apache Kafka enables asynchronous communication between the participating microservices.

### Apache Avro

Avro provides structured serialization for the main business events exchanged through Kafka.

### Choreography Saga

The order workflow uses a choreography-based Saga pattern without a central transaction orchestrator.

### Compensation Handling

Payment failure triggers inventory compensation and ultimately changes the order status to `CANCELLED`.

### Fault Tolerance

Resilience4j provides Circuit Breaker, Retry, Time Limiter, and Fallback capabilities.

### Observability

Micrometer and Prometheus provide metrics, while Micrometer Tracing and Zipkin provide distributed tracing.

### Containerization

Docker packages the backend services into portable runtime containers.

### Kubernetes

Kubernetes is used to deploy and manage the distributed application components.

### Security

JWT-based authentication protects the API Gateway and prevents unauthenticated access to protected APIs.

### Frontend Integration

A responsive Bootstrap-based frontend communicates with the backend through REST APIs exposed by the API Gateway.

### End-to-End Verification

The project was verified through authenticated API testing and Kubernetes-based distributed workflow testing.

Both successful payment and payment-failure compensation scenarios were demonstrated.

---
## Project Status

The project has completed the major distributed e-commerce implementation and verification stages.

### Implemented Components

- Microservices architecture
- Eureka service discovery
- Spring Cloud Config
- Spring Cloud Gateway
- JWT-based API authentication
- Apache Kafka event-driven communication
- Apache Avro event serialization
- Choreography Saga
- Payment-failure compensation
- Inventory reservation and release
- Resilience4j fault tolerance
- Micrometer metrics
- Prometheus monitoring
- Micrometer Tracing
- Zipkin tracing configuration
- PostgreSQL persistence
- Docker containerization
- Kubernetes deployment manifests
- Responsive frontend
- Service-level tests
- API-level verification
- End-to-end distributed workflow verification

### Verified Workflows

The successful payment workflow was verified from order creation through final `PAID` status.

The payment-failure workflow was verified from order creation through inventory compensation and final `CANCELLED` status.

Inventory restoration was also verified after the compensation workflow.

### Deployment Verification

The Kubernetes environment was used to verify:

- Service availability
- Eureka registration
- Kafka connectivity
- API Gateway health
- JWT authentication
- REST API communication
- Distributed Saga processing
- Payment success
- Payment failure
- Inventory compensation
- Order status transitions

### Current State

The backend distributed system and frontend integration are implemented and verified.

The repository contains the source code, configuration, Dockerfiles, Kubernetes manifests, tests, and frontend required for the project.

Sensitive credentials remain external to the repository.

---
## Conclusion

Zaalima Project 2 demonstrates a distributed e-commerce system built around modern microservices and event-driven architecture principles.

The project combines Spring Boot microservices, Eureka service discovery, centralized configuration, Spring Cloud Gateway, Apache Kafka, Apache Avro, Choreography Saga, Resilience4j, Micrometer, Prometheus, Zipkin, PostgreSQL, Docker, and Kubernetes.

The distributed order workflow was verified through both successful and failure scenarios.

In the successful scenario, an order moves from `CREATED` through inventory reservation and successful payment to the final `PAID` state.

In the failure scenario, a payment failure produces a compensation flow that releases the reserved inventory and transitions the order to `CANCELLED`.

The project also includes a responsive frontend that communicates with the backend through authenticated REST APIs exposed by the API Gateway.

Overall, the project demonstrates how independently deployable microservices can communicate asynchronously, handle distributed transaction failures through compensation, expose operational metrics and tracing information, and run together in a containerized Kubernetes environment.

---
