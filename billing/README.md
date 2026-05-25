# Billing Service

Internal Saga participant for the energy marketplace.
Manages simulated user balances and processes payment commands from Trade Service via RabbitMQ.

Not exposed through the API Gateway — internal only.

## Stack

- Python 3.12 + FastAPI
- SQLAlchemy 2 (async) + Alembic + PostgreSQL
- aio-pika (RabbitMQ)
- Hexagonal architecture

## Setup

```bash
pip install -r requirements.txt
cp .env.example .env
```

## Run locally

```bash
# start infrastructure
docker compose up billing-db rabbitmq -d

# run migrations
alembic upgrade head

# seed test accounts (optional)
psql billing_db -f infra/seed.sql

# start service
uvicorn src.main:app --reload
```

## Run via Docker Compose (full system)

```bash
docker compose up
```

## Tests

```bash
# unit tests — no infrastructure needed
pytest tests/unit -v
```

## Internal endpoints

| Method | Path | Description |
|---|---|---|
| GET | /health | Health check |
| GET | /billing/accounts/{userId} | User balance |
| GET | /billing/receipts/{tradeId} | Trade receipt |

## Commands received

Consumed from `billing.commands` queue, bound to `billing.exchange`:

| Routing key | Source command | Action |
|---|---|---|
| billing.payment.authorize | AuthorizePaymentCommand | Reserve funds from buyer |
| billing.payment.settle | SettlePaymentCommand | Transfer funds buyer → seller |
| billing.receipt.generate | GenerateReceiptCommand | Generate trade receipt |

## Events published

Published to `billing.exchange`:

| Routing key | Matches Java class | Trigger |
|---|---|---|
| billing.payment.authorized | HandlePaymentAuthorizedCommand | Authorization success |
| billing.payment.authorization.failed | — | Insufficient funds |
| billing.payment.settled | HandlePaymentSettledCommand | Settlement success |
| billing.payment.settlement.failed | — | Settlement error |
| billing.receipt.generated | HandleReceiptGeneratedCommand | Receipt created |

## Message contract — Money

All amounts follow the Java Money value object shape:

```json
{"amount": "100.00", "currency": "EUR"}
```

## Key design decisions

- IDs are integers (matching Java `Long`)
- `SettlePaymentCommand` has no buyerId/sellerId — looked up from stored authorization
- `GenerateReceiptCommand` is a separate command, not auto-triggered after settle
- All operations are idempotent — safe to retry
- Failed messages go to `billing.commands.dlq` for manual inspection
