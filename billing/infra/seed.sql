-- Seed test accounts for local development
-- Run after: alembic upgrade head
-- Usage: psql billing_db -f infra/seed.sql

INSERT INTO accounts (user_id, balance_amount, balance_currency, reserved_amount, reserved_currency, created_at)
VALUES
    (1, 5000.00, 'EUR', 0.00, 'EUR', NOW()),
    (2, 3000.00, 'EUR', 0.00, 'EUR', NOW()),
    (3,  500.00, 'EUR', 0.00, 'EUR', NOW()),
    (4,    0.00, 'EUR', 0.00, 'EUR', NOW())
ON CONFLICT (user_id) DO NOTHING;

-- user 1: 5000 EUR  — normal buyer
-- user 2: 3000 EUR  — normal buyer or seller
-- user 3:  500 EUR  — low balance (test near-failure cases)
-- user 4:    0 EUR  — always fails authorization
