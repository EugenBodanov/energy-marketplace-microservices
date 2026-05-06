# Listing Service

Energy Marketplace Listing Service - Manages energy listings and reservations in the Saga workflow.

## Architecture Overview

This service follows **Hexagonal Architecture** (Ports & Adapters pattern), which cleanly separates:

- **Domain Layer**: Core business logic, entities, value objects, exceptions
- **Application Layer**: Use cases, commands, events, result objects, inbound/outbound ports
- **Adapter Layer**: REST controllers, persistence, messaging, mappers

### Directory Structure

```
listing/
├── src/main/java/com/energy/marketplace/listing/
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── web/               # REST endpoints, DTOs, web mappers
│   │   │   └── messaging/         # RabbitMQ command listeners
│   │   └── out/
│   │       ├── persistence/       # JPA repository, persistence mappers, adapter
│   │       └── messaging/         # RabbitMQ event publishers
│   ├── application/
│   │   ├── command/               # Command objects for use cases
│   │   ├── event/                 # Domain events for RabbitMQ
│   │   ├── port/
│   │   │   ├── in/               # Inbound ports (use cases)
│   │   │   └── out/              # Outbound ports (driven adapters)
│   │   ├── result/               # Result DTOs
│   │   └── service/              # Use case service implementations
│   ├── domain/
│   │   ├── exception/            # Custom exceptions
│   │   ├── model/                # Domain entities (Listing)
│   │   └── valueObject/          # Value objects (Price, Capacity, Status)
│   ├── config/                   # Spring configuration (Beans, RabbitMQ)
│   └── ListingApplication.java   # Main application entry point
├── src/main/resources/
│   └── application.yaml          # Application properties
├── pom.xml                       # Maven dependencies
└── Dockerfile                    # Docker image configuration
```

## Key Features

### 1. List ing Management
- **Create Listing**: Sellers can create energy listings with price and capacity
- **Update Listing**: Modify available listings
- **Delete Listing**: Cancel a listing (only if available)
- **Search Listings**: Filter by status, seller, with pagination

### 2. Saga Integration
- **Reserve Listing**: Called by Trade Service via RabbitMQ during trade saga
- **Release Listing**: Compensation step if trade fails
- **Close Listing**: Mark listing as completed after successful trade

### 3. RabbitMQ Integration
**Commands Consumed**:
- `listing.reserve.command` → ReserveListingCommand
- `listing.release.command` → ReleaseListingCommand
- `listing.close.command` → CloseListingCommand

**Events Published**:
- `listing.created.event` → ListingCreatedEvent
- `listing.reserved.event` → ListingReservedEvent
- `listing.reservation_failed.event` → ListingReservationFailedEvent
- `listing.released.event` → ListingReleasedEvent
- `listing.closed.event` → ListingClosedEvent

## Technology Stack

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.0
- **ORM**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL
- **Message Broker**: RabbitMQ (Spring AMQP)
- **Build**: Maven
- **Deployment**: Docker

## API Endpoints

### REST Endpoints

```
POST   /api/v1/listings              # Create listing
GET    /api/v1/listings              # Search listings (with filters)
GET    /api/v1/listings/{listingId}  # Get single listing
PUT    /api/v1/listings/{listingId}  # Update listing
DELETE /api/v1/listings/{listingId}  # Delete listing
```

### Search Filters

- `?status=AVAILABLE` - Filter by status
- `?sellerId=123` - Filter by seller
- `?page=1&pageSize=10` - Pagination

## Domain Model

### Listing Entity

```
Listing {
  id: Long
  sellerId: Long
  title: String
  description: String
  price: ListingPrice
  capacity: Capacity
  status: ListingStatus               # AVAILABLE, RESERVED, COMPLETED, CANCELLED
  reservationReference: Long          # Trade ID when reserved
  createdAt: Instant
  updatedAt: Instant
}
```

### Value Objects

- **ListingPrice**: Immutable price with amount and currency
- **Capacity**: Immutable capacity with value and unit (kWh)
- **ListingStatus**: Enum for listing states

## Configuration

### application.yaml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update           # Auto-create schema
  datasource:
    url: jdbc:postgresql://localhost:5432/listings_db
    username: postgres
    password: postgres
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

server:
  port: 8082
  servlet:
    context-path: /api/v1
```

## Running Locally

### Prerequisites
- Java 21
- PostgreSQL running on localhost:5432
- RabbitMQ running on localhost:5672

### Build & Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/listing-0.0.1-SNAPSHOT.jar

# Or run directly with Maven
mvn spring-boot:run
```

### Docker Deployment

```bash
# Build Docker image
docker build -t listing-service:latest .

# Run Docker container
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/listings_db \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  listing-service:latest
```

## Hexagonal Architecture Design

### Dependency Flow

```
REST Request → Controller → Mapper → Use Case (Port) → Service → Domain Model
                                              ↓
                                          Outbound Ports
                                              ↓
                                    Persistence/Messaging Adapters
```

### Key Principles

1. **Dependency Inversion**: Application layer depends on abstractions (ports), not concrete adapters
2. **No Circular Dependencies**: Outer layers depend on inner layers
3. **Testability**: Business logic isolated from infrastructure
4. **Flexibility**: Easy to swap adapters (e.g., different databases, messaging systems)

### Example: Creating a Listing

1. POST request → ListingController
2. Controller calls CreateListingUseCase (Port)
3. Service implements port → validates → calls domain factory
4. Domain creates Listing aggregate
5. Service calls SaveListingPort (outbound)
6. Persistence Adapter saves to database
7. Service publishes ListingCreatedEvent via PublishListingEventPort
8. Event Publisher sends to RabbitMQ

## State Transitions

```
AVAILABLE → RESERVED (when trade starts)
RESERVED → AVAILABLE (when trade fails/cancelled - release)
RESERVED → COMPLETED (when trade succeeds - close)
ANY → CANCELLED (manual cancellation)
```

## Error Handling

Custom exceptions for robust error handling:
- `ListingNotFoundException`: Listing doesn't exist
- `ListingInvalidStateException`: Operation not valid for current state
- `InsufficientCapacityException`: Not enough capacity available

## Future Enhancements

- [ ] Implement command deserialization with type headers
- [ ] Add comprehensive integration tests with Testcontainers
- [ ] Add metrics/monitoring with Micrometer
- [ ] Implement optimistic locking for concurrency
- [ ] Add audit logging for all state changes
- [ ] Support partial reservations
- [ ] Add advanced search (date ranges, price ranges)

## Testing Strategy

- **Unit Tests**: Domain model, service logic
- **Integration Tests**: Controller + Service + Repository with @SpringBootTest
- **End-to-End Tests**: Full workflow with Testcontainers (PostgreSQL + RabbitMQ)

## Contributing

Follow these guidelines:
1. Maintain hexagonal architecture pattern
2. Write tests for new features
3. Update documentation
4. Use meaningful commit messages

