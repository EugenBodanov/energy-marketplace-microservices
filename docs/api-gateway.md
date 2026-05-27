# API Gateway Implementation: Spring Cloud Gateway Guide

## What Has Been Built

The API Gateway has been implemented as a centralized reactive entry point following the **Reverse Proxy and API Gateway patterns**. It abstracts the distributed nature of the microservices stack, providing a single domain interface for clients.

### Project Structure
```
api-gateway/
├── src/main/java/com/energy/marketplace/gateway/
│   ├── adapter/
│   │   └── in/
│   │       └── HealthCheckController.java  → Gateway status & routing info metadata
│   ├── config/
│   │    ├── AuthenticationFilter.java       → Global JWT observation & interceptor placeholder
│   │    ├── ErrorFilter.java                → WebFlux global exception handler & JSON response mapper
│   │    └── LoggingFilter.java              → Netty reactive in/out traffic tracking diagnostics
│   └── ApiGatewayApplication.java      → Dynamic Path-Pattern RouteLocator & main bootstrap
├── src/main/resources/
│   └── application.yaml                    → Profile management, CORS configurations, and service routes
└── Dockerfile                              → Multi-stage JRE optimization build file
```
## Key Features Implemented

### 1. **Dynamic Path Pattern Routing**
Instead of manually mapping individual REST endpoints, the gateway uses highly optimized wildcard matching (`**`). This route mapping decouples edge routing from downstream controllers, allowing teams to add backend endpoints without modifications to the gateway layer:
- `/users/**`   → Dynamically routed to `user-service`
- `/listings/**`→ Dynamically routed to `listing-service`
- `/trades/**`  → Dynamically routed to `trade-service`

### 2. **Global Cross-Origin Resource Sharing (CORS)**
Configured at the gateway edge inside `application.yaml` to permit decoupling of the frontend user interface. It intercepts preflight options requests natively:
- **Allowed Origins:** `*` (Enables seamless local browser UI development)
- **Allowed Methods:** `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- **Max Age:** `3600` seconds

### 3. **Reactive Diagnostic Logging & Exception Handling**
- **LoggingFilter:** A global high-priority filter mapping every incoming request and downstream response status directly to the console buffer.
- **ErrorFilter:** Overrides WebFlux's native reactive error handling. It catches downstream routing disruptions or timeouts, converting raw stack traces into safe, standardized JSON responses containing timestamp, status code, error classification, and request URI path context.

## Technology

| Area               | Current Implementation                                            |
| ------------------ | ----------------------------------------------------------------- |
| Java               | 21                                                                |
| Framework          | Spring Boot 3.2.0 & Spring Cloud Gateway                          |
| Server Runtime     | Embedded Netty (Reactive, Non-blocking I/O)                       |
| Observability      | Micrometer Tracing with ObservedRequestHttpHeadersFilter          |

## Getting Started

### Prerequisites
- Docker and Docker Desktop installed and running.

### Container Deployment
The entire marketplace ecosystem is managed as a unified network cluster using container orchestration.

```bash
# 1. Build the gateway container image manually from the repository root
cd api-gateway
docker build -t energy-community-marketplace/api-gateway:local .

# 2. Boot up the entire stack from the project root folder
cd ..
docker compose up -d
````
### Verify
You can verify that the API Gateway is successfully routing traffic to the internal microservices by executing this registration call in your terminal:

````bash
curl -i -X POST "http://localhost:8080/users/register" ^
-H "Content-Type: application/json" ^
-d "{\"name\":\"developer_test\",\"email\":\"test@energy-community.at\",\"rawPassword\":\"SecurePassword123\",\"role\":\"consumer\"}"
````