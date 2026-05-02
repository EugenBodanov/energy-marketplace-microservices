# Milestone 1 — Microservices Stack and Architecture Decisions

## 1. General Stack

| Area               | Decision                    | Short justification                                                                      |
| ------------------ | --------------------------- | ---------------------------------------------------------------------------------------- |
| Repository         | Monorepo                    | Easier development, review, Docker Compose setup, and documentation for a study project. |
| Language           | Java 21                     | Stable enterprise language for service-oriented backend systems.                         |
| Framework          | Spring Boot 3               | Strong support for REST, validation, persistence, messaging, and testing.                |
| Architecture style | Hexagonal architecture      | Keeps business logic separated from REST, database, and message broker adapters.         |
| Database           | PostgreSQL                  | Reliable relational database for users, listings, trades, ledger entries, and receipts.  |
| Persistence        | Spring Data JPA / Hibernate | Fast implementation of repository and entity layers.                                     |
| Message broker     | RabbitMQ                    | Simpler than Kafka for a study Saga workflow; supports durable queues, retries, and DLQ. |
| API documentation  | OpenAPI / Swagger           | Makes service contracts visible and easy to test.                                        |
| Local deployment   | Docker Compose              | Enough for local demonstration without Kubernetes complexity.                            |
| Testing            | JUnit 5 + Testcontainers    | Suitable for integration tests with PostgreSQL and RabbitMQ.                             |

---

## 2. API Gateway

**Service name:** `api-gateway`

**Stack:** Spring Cloud Gateway + Spring Boot 3  
**Why:** Ready-made routing layer for Spring-based microservices; supports token forwarding, filters, and gateway-level validation.

**Responsibility:**

- Single entry point for all clients.
- Route requests to internal services.
- Forward authentication data/tokens.
- Apply basic request validation and future rate limiting.
- No business workflow orchestration.
- Limited API Composition layer for read-only client-facing views

**Public routes:**

```http
/api/users/**     -> user-service
/api/listings/**  -> listing-service
/api/trades/**    -> trade-service
```

**Important decision:** Billing Service is not exposed through the API Gateway. It is used only inside the trade workflow.

---

## 3. User Service

**Service name:** `user-service`

**Stack:** Spring Boot 3 + Spring Web + Spring Data JPA + PostgreSQL + JWT/simple token auth  
**Why:** User registration, login, validation, and persistence are simple REST + database tasks.

**Responsibility:**

- Register users.
- Authenticate users.
- Store user profile and role.
- Validate users before trade execution.

**Database:** `users_db`

**Main endpoints:**

```http
POST /users/register
POST /users/login
GET  /users/{id}
GET  /users/{id}/validate
```

**Communication:**

- Called by API Gateway for user-facing operations.
- Called synchronously by Trade Service for user validation.

---

## 4. Listing Service

**Service name:** `listing-service`

**Stack:** Spring Boot 3 + Spring Web + Spring Data JPA + PostgreSQL + RabbitMQ  
**Why:** Needs REST for listing management and RabbitMQ for Saga commands such as reserve, release, and close listing.

**Responsibility:**

- Create, update, delete energy offers.
- Search and filter available listings.
- Reserve listing during trade Saga.
- Release reservation during compensation.
- Close listing after successful trade.

**Database:** `listings_db`

**Main REST endpoints:**

```http
POST   /listings
GET    /listings
GET    /listings/{id}
PUT    /listings/{id}
DELETE /listings/{id}
```

**RabbitMQ commands consumed:**

```text
ReserveListingCommand
ReleaseListingCommand
CloseListingCommand
```

**RabbitMQ events published:**

```text
ListingReservedEvent
ListingReservationFailedEvent
ListingReleasedEvent
ListingClosedEvent
```

---

## 5. Trade Service

**Service name:** `trade-service`

**Stack:** Spring Boot 3 + Spring Web + Spring Data JPA + PostgreSQL + RabbitMQ + WebClient/OpenFeign  
**Why:** This service owns the Saga workflow, stores trade state, calls User Service synchronously, and communicates with Listing/Billing asynchronously.

**Responsibility:**

- Start trade execution.
- Validate buyer/seller through User Service.
- Orchestrate the full trade Saga.
- Store trade state and trade state history.
- Coordinate Listing Service and Billing Service.
- Trigger compensation on failure.

**Database:** `trades_db`

**Main endpoints:**

```http
POST /trades
GET  /trades/{id}
GET  /trades
GET  /trades/{id}/receipt
```

**Synchronous outgoing REST call:**

```http
GET /users/{id}/validate
```

**RabbitMQ commands published:**

```text
ReserveListingCommand
AuthorizePaymentCommand
SettlePaymentCommand
ReleaseListingCommand
CloseListingCommand
RollbackPaymentCommand
```

**RabbitMQ events consumed:**

```text
ListingReservedEvent
ListingReservationFailedEvent
PaymentAuthorizedEvent
PaymentAuthorizationFailedEvent
PaymentSettledEvent
PaymentSettlementFailedEvent
PaymentRolledBackEvent
ReceiptGeneratedEvent
```

**Key decision:** Trade Service is the Saga orchestrator. API Gateway only routes requests and does not coordinate business workflows.

---

## 6. Billing Service

**Service name:** `billing-service`

**Stack:** Spring Boot 3 + Spring Data JPA + PostgreSQL + RabbitMQ  
**Why:** Billing is internal to the Saga and processes asynchronous payment commands using a simulated ledger.

**Responsibility:**

- Manage simulated internal user balances.
- Authorize payment.
- Settle payment.
- Roll back payment reservation.
- Generate receipt.
- Store failed payment attempts if needed.

**Database:** `billing_db`

**Public endpoints:**

```text
No public API Gateway route.
```

**Internal endpoints:**

```http
GET /billing/accounts/{userId}
GET /billing/receipts/{tradeId}
```

**RabbitMQ commands consumed:**

```text
AuthorizePaymentCommand
SettlePaymentCommand
RollbackPaymentCommand
```

**RabbitMQ events published:**

```text
PaymentAuthorizedEvent
PaymentAuthorizationFailedEvent
PaymentSettledEvent
PaymentSettlementFailedEvent
PaymentRolledBackEvent
ReceiptGeneratedEvent
```

**Key decision:** Billing Service replaces a real Payment Service. No Stripe/bank integration is needed; the project uses a simulated ledger.

---

## 7. Message Broker

**Technology:** RabbitMQ

**Stack:** RabbitMQ + Spring AMQP

**Why:** RabbitMQ is enough for this project because the Saga needs durable command/event delivery, retry support, and simple queue-based communication.

**Exchange:**

```text
trade.saga.exchange
```

**Queues:**

```text
listing.commands.queue
billing.commands.queue
trade.events.queue
```

**Example routing keys:**

```text
listing.reserve.command
listing.release.command
listing.close.command

billing.authorize.command
billing.settle.command
billing.rollback.command

listing.reserved.event
listing.reservation_failed.event
payment.authorized.event
payment.authorization_failed.event
payment.settled.event
payment.settlement_failed.event
receipt.generated.event
```

**Broker responsibility:**

- Deliver Saga commands to Listing and Billing services.
- Deliver Saga events back to Trade Service.
- Reduce direct synchronous dependencies.
- Support recovery after temporary service failures.

---

## 8. Final Architecture Decisions

| Decision               | Selected option                       |
| ---------------------- | ------------------------------------- |
| System style           | Microservices                         |
| Internal service style | Hexagonal architecture                |
| Repository structure   | Monorepo                              |
| Gateway                | Spring Cloud Gateway                  |
| Service framework      | Spring Boot 3                         |
| Database model         | Database per service                  |
| Sync communication     | REST                                  |
| Async communication    | RabbitMQ                              |
| Distributed workflow   | Saga orchestration                    |
| Saga owner             | Trade Service                         |
| Authentication owner   | User Service                          |
| Payment model          | Billing Service with simulated ledger |
| Local deployment       | Docker Compose                        |
