# Saga Orchestration Test Suite

This directory contains test scenarios and orchestration scripts used to validate the distributed Saga lifecycle across the marketplace microservices:

* API Gateway
* User Service
* Listing Service
* Trade Service
* Billing Service

The automation verifies service startup, infrastructure readiness, broker connectivity, and end-to-end Saga execution.

---

## Execution Requirements

### Important: Run from the Project Root

All Python automation scripts must be executed from the project root directory.

Do **not** execute scripts directly from within the `scripts/` folder, as relative paths, Docker Compose lookups, configuration resolution, and test asset discovery depend on the repository root.

### Correct Usage

```bash
python ./scripts/test_saga.py [flag]
```

### Alignment with .env

The python scripts url configurations have to align with the `.env` file in the project root. If you have modified the `.env` file, ensure that the test suite is set up accordingly.
In my case, the `.env` file is configured to use the following ports:

* API_GATEWAY_PORT=8080
* USER_SERVICE_PORT=8081
* LISTING_SERVICE_PORT=8082
* TRADE_SERVICE_PORT=8083
* BILLING_SERVICE_PORT=8084
---

## Command Line Flags

The orchestration engine provides optional startup behavior.

| Flag      | Description                                                                                                 |
| --------- | ----------------------------------------------------------------------------------------------------------- |
| None      | Uses the currently running Docker ecosystem and performs validation only.                                   |
| `--reset` | Performs a complete infrastructure reset, removes volumes, rebuilds images, and starts a fresh environment. |

### Examples

#### Fast Validation

```bash
python ./scripts/test_saga.py
```

Uses existing containers and skips unnecessary rebuilds.

#### Clean Environment Validation

```bash
python ./scripts/test_saga.py --reset
```

Performs:

```bash
docker compose down -v
docker compose up -d --build
```

This is useful when validating:

* Empty RabbitMQ queues
* Fresh database state
* Flyway migrations
* Infrastructure startup behavior
* Full Saga initialization

---

## Infrastructure Overview

The test suite polls service health endpoints before submitting test payloads.

### Health Verification

Before executing any Saga scenario, the suite polls:

```text
/actuator/health
```

for up to 120 seconds.

This startup grace period allows:

* RabbitMQ initialization
* Database availability
* Service registration
* Broker connection establishment
* Flyway migration completion

---

## Troubleshooting

### RabbitMQ Startup Failure During `--reset`

#### Symptom

After executing a full reset, the RabbitMQ container may terminate unexpectedly.

Container logs show:

```text
Application rabbitmq_prelaunch exited with reason:
"Error when reading /var/lib/rabbitmq/.erlang.cookie: eacces"
```

#### Cause

This issue is typically related to Docker volume ownership and file permission inconsistencies.

It is most commonly observed on:

* Docker Desktop for Windows
* WSL2-backed Docker environments
* Interrupted container shutdowns
* Rapid restart sequences

RabbitMQ requires strict permissions on the `.erlang.cookie` file. If the file ownership or permissions become invalid inside the persisted Docker volume, RabbitMQ refuses to start.

#### Recommended Resolution

Execute:

```bash
docker compose down -v --remove-orphans
docker volume prune -f
docker compose up -d --build
```

If the issue persists:

```bash
docker compose down
docker system prune -a --volumes
```

Then rebuild the environment completely.

#### Additional Notes

The issue is usually not caused by application code.

Instead, it originates from persisted Docker volume metadata and filesystem permission translation between Windows, WSL2, and Linux containers.

In most cases, removing the affected RabbitMQ volume resolves the problem.
