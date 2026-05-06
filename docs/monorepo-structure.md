# Monorepo Structure

## Decision

The project uses a **monorepo**: all microservices, infrastructure files, shared contracts, and documentation are stored in one Git repository.

## Why Monorepo

- Easier to run the whole system locally.
- One `docker-compose.yml` for all services.
- Easier project review and demonstration.
- Shared documentation and architecture decisions are kept in one place.

## Repository Structure

Planned monorepo shape:

```text
energy-marketplace/
├── README.md
├── docker-compose.yml
├── .env.example
│
├── api-gateway/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── user-service/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── listing-service/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── trade-service/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── billing-service/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
│
├── shared-contracts/
│   ├── rest/
│   └── messaging/
│
├── infra/
│   ├── postgres/
│   └── rabbitmq/
│
└── docs/

```

## Service Independence

Each microservice has its own:

- source code
- configuration
- Dockerfile
- database/schema
- API contract
- responsibility

Services do not access each other's databases directly.

## Shared Contracts

`shared-contracts/` is used only for technical contracts, for example:

- REST DTOs
- RabbitMQ message schemas
- common error formats

Business logic should not be placed in `shared-contracts/`.

## Important Rule

The monorepo is used for development convenience only. Architecturally, the services remain independent microservices.
