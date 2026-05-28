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
| GET | /billing/receipts/{receiptId} | Trade receipt |

## Commands received

Consumed from `billing.commands.queue`, bound to `trade.saga.exchange`:

| Routing key | Source command | Action |
|---|---|---|
| billing.authorize.command | AuthorizePaymentCommand | Reserve funds from buyer |
| billing.settle.command | SettlePaymentCommand | Transfer funds buyer to seller |
| billing.generate_receipt.command | GenerateReceiptCommand | Generate trade receipt |
| billing.cancel_payment.command | CancelPaymentCommand | Release reserved buyer funds |

## Events published

Published to `trade.saga.exchange`:

| Routing key | Matches Java class | Trigger |
|---|---|---|
| payment.authorized.event | HandlePaymentAuthorizedCommand | Authorization success |
| payment.authorization_failed.event | HandlePaymentAuthorizationFailedCommand | Insufficient funds |
| payment.settled.event | HandlePaymentSettledCommand | Settlement success |
| payment.settlement_failed.event | HandlePaymentSettlementFailedCommand | Settlement error |
| receipt.generated.event | HandleReceiptGeneratedCommand | Receipt created |
| payment.rolled_back.event | HandleCancelPaymentSuccess / HandleCancelPaymentFailed | Reserved funds released or release failed |

## Message contract - Money

Trade commands send most amounts as flat fields:

```json
{"amount": "100.00", "currency": "EUR"}
```

`CancelPaymentCommand` may send a nested Money object; Billing accepts both flat and nested shapes.

## Key design decisions

- IDs are integers (matching Java `Long`)
- `SettlePaymentCommand` has no buyerId/sellerId — looked up from stored authorization
- `GenerateReceiptCommand` is a separate command, not auto-triggered after settle
- Outgoing events include `eventType` because Trade routes inside its listener by that field
- All operations are idempotent — safe to retry
- Failed messages go to `billing.commands.dlq` for manual inspection
