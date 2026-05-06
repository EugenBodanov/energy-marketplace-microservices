# User Service

This document describes the current implementation of the `user` microservice.

## Current Scope

The `user` service is implemented in the `user/` module. It owns:

- user registration
- user login
- user lookup by id
- user validation for trade-related flows
- password hashing
- JWT issuing
- user persistence in PostgreSQL
- schema migration through Flyway
- OpenAPI specification and Swagger UI

The service currently does not implement:

- request authentication or authorization filters
- user status management endpoints
- admin user creation through the public API
- custom REST error response mapping

## Technology

| Area             | Current implementation                                            |
| ---------------- | ----------------------------------------------------------------- |
| Java             | 21                                                                |
| Framework        | Spring Boot 4.0.6                                                 |
| Web              | Spring WebMVC                                                     |
| Persistence      | Spring Data JPA, Hibernate                                        |
| Database         | PostgreSQL                                                        |
| Migrations       | Flyway with `spring-boot-flyway` and `flyway-database-postgresql` |
| Validation       | Jakarta Bean Validation                                           |
| Password hashing | BCrypt                                                            |
| Token issuing    | Spring Security OAuth2 JOSE `JwtEncoder`, HS256                   |
| API docs         | springdoc-openapi WebMVC UI                                       |
| Tests            | JUnit 5, Mockito, AssertJ, Testcontainers PostgreSQL              |

## Runtime Configuration

Configuration is stored in `user/src/main/resources/application.yaml`.

| Property                     | Environment variable         | Default                                     |
| ---------------------------- | ---------------------------- | ------------------------------------------- |
| `server.port`                | `SERVER_PORT`                | `8080`                                      |
| `spring.datasource.url`      | `USER_DB_URL`                | `jdbc:postgresql://localhost:5432/users_db` |
| `spring.datasource.username` | `USER_DB_USERNAME`           | `postgres`                                  |
| `spring.datasource.password` | `USER_DB_PASSWORD`           | `postgres`                                  |
| `app.jwt.secret`             | `APP_JWT_SECRET`             | `local-development-user-service-jwt-secret` |
| `app.jwt.issuer`             | `APP_JWT_ISSUER`             | `user-service`                              |
| `app.jwt.expiration-minutes` | `APP_JWT_EXPIRATION_MINUTES` | `60`                                        |
| `springdoc.api-docs.path` | `SPRINGDOC_API_DOCS_PATH` | `/v3/api-docs` |
| `springdoc.swagger-ui.path` | `SPRINGDOC_SWAGGER_UI_PATH` | `/swagger-ui.html` |
| `springdoc.swagger-ui.operations-sorter` | `SPRINGDOC_SWAGGER_UI_OPERATIONS_SORTER` | `method` |
| `springdoc.swagger-ui.tags-sorter` | `SPRINGDOC_SWAGGER_UI_TAGS_SORTER` | `alpha` |

The JWT secret must be at least 32 characters long.

JPA uses `spring.jpa.hibernate.ddl-auto=validate`, so Hibernate validates the schema but does not create or update it. Flyway is responsible for creating the schema before JPA validation.

## Local Deployment

`docker-compose.yml` currently defines:

- `user-db`: PostgreSQL 16 Alpine, database `users_db`
- `user-service`: builds from `./user`, listens on port `8080`

The service container waits for the database healthcheck and connects through:

```text
jdbc:postgresql://user-db:5432/users_db
```

## Architecture

The module follows a hexagonal structure:

```text
com.energy.marketplace.user
|-- adapter
|   |-- in.web          REST controller, DTOs, web mapper
|   `-- out
|       |-- persistence JPA entity, repository, persistence adapter
|       `-- security    BCrypt password adapter, JWT token adapter
|-- application
|   |-- command         use case input records and validation purpose enum
|   |-- port.in         use case interfaces
|   |-- port.out        persistence, hashing, and token ports
|   |-- result          use case output records
|   `-- service         application use case implementations
|-- config              bean wiring
`-- domain              User aggregate, value objects, domain exceptions
```

## Domain Model

`User` contains:

- `id`
- `name`
- `email`
- `password` containing the password hash
- `role`
- `status`

Supported roles:

- `ADMIN`
- `CONSUMER`
- `PROSUMER`

Supported statuses:

- `ACTIVE`
- `INACTIVE`
- `BLOCKED`

Registration through the public API accepts `CONSUMER` and `PROSUMER`. `ADMIN` exists in the enum and database constraint, but the public registration mapper rejects it.

Energy capability rules:

| Role       | Status                  | Can buy energy | Can sell energy |
| ---------- | ----------------------- | -------------- | --------------- |
| `CONSUMER` | `ACTIVE`                | yes            | no              |
| `PROSUMER` | `ACTIVE`                | yes            | yes             |
| any role   | `INACTIVE` or `BLOCKED` | no             | no              |

`CONSUMER` and `PROSUMER` are roles, not permissions. Trade permissions are derived from role and status in the domain model.

## Database Schema

Flyway migrations are stored in:

```text
user/src/main/resources/db/migration
```

Current migration:

```text
V1__create_users_table.sql
```

It creates the `users` table:

| Column          | Type              | Notes                                                    |
| --------------- | ----------------- | -------------------------------------------------------- |
| `id`            | `BIGINT` identity | primary key                                              |
| `name`          | `VARCHAR(255)`    | required                                                 |
| `email`         | `VARCHAR(255)`    | required, unique                                         |
| `password_hash` | `VARCHAR(255)`    | required                                                 |
| `role`          | `VARCHAR(32)`     | required, constrained to `ADMIN`, `CONSUMER`, `PROSUMER` |
| `status`        | `VARCHAR(32)`     | required, constrained to `ACTIVE`, `INACTIVE`, `BLOCKED` |

Spring Boot 4 requires the `spring-boot-flyway` module for Flyway auto-configuration. The project also includes `flyway-database-postgresql` for PostgreSQL support.

## REST API

All endpoints are rooted at:

```http
/api/v1/users
```

## OpenAPI and Swagger UI

OpenAPI support is provided by `springdoc-openapi-starter-webmvc-ui`.

Available documentation endpoints:

```http
GET /swagger-ui.html
GET /v3/api-docs
GET /v3/api-docs/user-service
GET /v3/api-docs.yaml
```

The `user-service` OpenAPI group is configured in `OpenApiConfig` and matches only:

```text
/api/v1/users/**
```

The OpenAPI metadata is defined in `OpenApiConfig`:

- title: `User Service API`
- description: `User registration, login, lookup, and trade validation API.`
- version: `v1`

Springdoc generates operation and schema details from Spring MVC mappings, record DTOs, and Jakarta Bean Validation metadata.

Springdoc warns when API docs and Swagger UI are enabled. Disable them in production through:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

### Register User

```http
POST /api/v1/users/register
```

Request:

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "rawPassword": "secret",
  "role": "PROSUMER"
}
```

Validation:

- `name` must not be blank
- `email` must not be blank and must be a valid email
- `rawPassword` must not be blank
- `role` must not be blank
- `role` must be `CONSUMER` or `PROSUMER` for public registration
- duplicate normalized email is rejected

Response:

```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "role": "PROSUMER",
  "status": "ACTIVE"
}
```

Registration normalizes the email value, hashes the password with BCrypt, and creates users with status `ACTIVE`.

### Login User

```http
POST /api/v1/users/login
```

Request:

```json
{
  "email": "alice@example.com",
  "rawPassword": "secret"
}
```

Response:

```json
{
  "userId": 1,
  "accessToken": "<jwt>",
  "role": "PROSUMER"
}
```

The current login flow validates email and password only. It does not currently block login by user status.

Issued JWT claims:

- `iss`
- `iat`
- `exp`
- `sub`
- `userId`
- `email`
- `role`

### Get User

```http
GET /api/v1/users/{userId}
```

Response:

```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "role": "PROSUMER",
  "status": "ACTIVE"
}
```

### Validate User

```http
GET /api/v1/users/{userId}/validate?purpose=PARTICIPATE_IN_TRADE
```

`purpose` is optional. The default is `PARTICIPATE_IN_TRADE`.

Supported values:

- `PARTICIPATE_IN_TRADE`
- `BUY_ENERGY`
- `SELL_ENERGY`

Response:

```json
{
  "userId": 1,
  "valid": true,
  "role": "PROSUMER",
  "status": "ACTIVE",
  "message": "User is valid"
}
```

Validation rules:

| Purpose                | Rule                                                          |
| ---------------------- | ------------------------------------------------------------- |
| `PARTICIPATE_IN_TRADE` | user status must be `ACTIVE`                                  |
| `BUY_ENERGY`           | user must be active and role must be `CONSUMER` or `PROSUMER` |
| `SELL_ENERGY`          | user must be active and role must be `PROSUMER`               |

Invalid validation results return `valid=false` with one of:

- `User cannot participate in trade`
- `User cannot buy energy`
- `User cannot sell energy`

If the user id does not exist, the application service throws `UserNotFoundException`.

## Tests

Current test coverage includes:

- domain value object and energy capability rules
- registration service behavior
- login service behavior
- get user service behavior
- validate user service behavior
- Spring Boot integration test with Testcontainers PostgreSQL
- OpenAPI documentation endpoint exposure

`UserApplicationTests` starts a PostgreSQL Testcontainer when Docker is available. Spring Boot then applies Flyway migrations during context startup, before Hibernate schema validation. The test verifies that migration `V1` was applied and that users can be persisted and loaded through the persistence adapter.

The integration test also checks that `/v3/api-docs/user-service` returns the generated OpenAPI document.

If Docker is not available, `UserApplicationTests` is disabled by a JUnit execution condition and logs a warning instead of failing the build.

Useful commands:

```powershell
cd user
.\mvnw.cmd test
.\mvnw.cmd test "-Dtest=UserApplicationTests"
```
