# Listing Service Implementation: Hexagonal Architecture Guide

## What Has Been Built

The Listing Service has been fully implemented following **Hexagonal Architecture** principles with complete separation of concerns:

### Project Structure

```
Domain Layer (Business Logic)
├── model/           → Listing entity with state management
├── valueObject/     → ListingPrice, Capacity enums for domain concepts
└── exception/       → Custom exceptions for error handling

Application Layer (Use Cases & Workflows)
├── command/         → Input objects (Create, Update, Reserve, Release, Close)
├── event/           → Domain events for async communication (RabbitMQ)
├── port/
│   ├── in/         → Use Case interfaces (driving/inbound ports)
│   └── out/        → Adapter interfaces (driven/outbound ports)
├── result/         → Output DTOs for responses
└── service/        → Use case implementations

Adapter Layer (Infrastructure & External Systems)
├── in/
│   ├── web/        → REST Controller, Request/Response DTOs, Web Mapper
│   └── messaging/  → RabbitMQ command listeners
└── out/
    ├── persistence/ → JPA Entity, Repository, Persistence Mapper & Adapter
    └── messaging/  → RabbitMQ event publisher

Configuration Layer
├── ListingBeanConfig    → Spring Bean declarations & dependency injection
├── RabbitMQConfig       → Message queues, exchanges, bindings
└── Application.yaml     → Environment configuration
```

## Key Features Implemented

### 1. **REST API (Inbound Adapter)**
- POST `/listings` - Create new listing
- PUT `/listings/{id}` - Update listing
- DELETE `/listings/{id}` - Delete listing
- GET `/listings/{id}` - Retrieve single listing
- GET `/listings` - Search with filters (status, sellerId, pagination)

### 2. **Persistence (Outbound Adapter)**
- JPA Entity mapping with PostgreSQL
- Repository with custom queries
- Persistence Mapper for domain-to-entity conversion

### 3. **Saga Integration (Inbound/Outbound Messaging)**
**RabbitMQ Commands (Inbound)**:
- `listing.reserve.command` → ReserveListingCommand
- `listing.release.command` → ReleaseListingCommand
- `listing.close.command` → CloseListingCommand

**RabbitMQ Events (Outbound)**:
- `listing.created.event` → ListingCreatedEvent
- `listing.reserved.event` → ListingReservedEvent
- `listing.reservation_failed.event` → ListingReservationFailedEvent
- `listing.released.event` → ListingReleasedEvent
- `listing.closed.event` → ListingClosedEvent

### 4. **Use Cases (Application Services)**
- CreateListingService - Creates new listing and publishes event
- UpdateListingService - Updates available listing
- DeleteListingService - Deletes listing
- GetListingService - Retrieves listing by ID
- SearchListingsService - Searches with pagination
- ReserveListingService - Reserves for trade + error handling
- ReleaseListingService - Releases from reservation (compensation)
- CloseListingService - Closes listing (success)

### 5. **Domain Model**
- Listing aggregate with state transitions
- ListingPrice & Capacity value objects (immutable, validated)
- ListingStatus enum (AVAILABLE, RESERVED, COMPLETED, CANCELLED)
- Business rule enforcement (state validation)
- Custom exceptions for error scenarios

## Getting Started

### Prerequisites
```bash
# Java 21
java -version

# PostgreSQL running
psql --version

# RabbitMQ running
# Visit http://localhost:15672 (guest/guest)
```

### Local Development
```bash
# Build
cd listing
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test
```

### Docker Deployment
```bash
# Build image
docker build -t listing-service:latest .

# Run with docker-compose (recommended)
# See root Docker Compose configuration
```

## Integration Points

### Inbound (Driving Force)
1. **REST Controller** - HTTP requests from API Gateway
2. **RabbitMQ Listener** - Message commands from Trade Service

### Outbound (Driven)
1. **JPA Repository** - PostgreSQL persistence
2. **RabbitMQ Publisher** - Saga events to Trade Service

## Configuration Reference

```yaml
# application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/listings_db
  rabbitmq:
    host: localhost
    port: 5672
    
server:
  port: 8082
  servlet:
    context-path: /api/v1
```
