from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import time
import http.client
import urllib.error
import urllib.request
import uuid
from decimal import Decimal
from pathlib import Path
from typing import Any


SCRIPT_PATH = Path(__file__).resolve()
PROJECT_ROOT = SCRIPT_PATH.parents[1] if SCRIPT_PATH.parent.name == "scripts" else SCRIPT_PATH.parent

GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")
USER_URL = os.getenv("USER_URL", "http://localhost:8085")
LISTING_URL = os.getenv("LISTING_URL", "http://localhost:8082")
TRADE_URL = os.getenv("TRADE_URL", "http://localhost:8081")
BILLING_URL = os.getenv("BILLING_URL", "http://localhost:8083")

TERMINAL_TRADE_STATUSES = {"COMPLETED", "FAILED", "COMPENSATION_FAILED"}
DIAGNOSTIC_SERVICES = (
    "rabbitmq",
    "user-service",
    "listing-service",
    "trade-service",
    "billing-service",
    "api-gateway",
)


class SagaTestError(RuntimeError):
    pass


def run_command(
    command: list[str],
    *,
    capture: bool = False,
    check: bool = True,
) -> subprocess.CompletedProcess[str]:
    result = subprocess.run(
        command,
        cwd=PROJECT_ROOT,
        text=True,
        capture_output=capture,
        check=False,
    )
    if check and result.returncode != 0:
        detail = result.stderr.strip() or result.stdout.strip()
        if len(detail) > 6000:
            detail = f"... output truncated ...\n{detail[-6000:]}"
        raise SagaTestError(
            f"Command failed ({result.returncode}): {' '.join(command)}\n{detail}"
        )
    return result


def docker_compose(*args: str, capture: bool = False, check: bool = True) -> subprocess.CompletedProcess[str]:
    return run_command(["docker", "compose", *args], capture=capture, check=check)


def print_command_output(title: str, command: list[str]) -> None:
    result = run_command(command, capture=True, check=False)
    output = (result.stdout or "") + (result.stderr or "")
    print(f"\n--- {title} ---")
    print(output.strip() or "(no output)")


def print_diagnostics() -> None:
    print("\n[!] Diagnostics snapshot")
    print_command_output("docker compose ps", ["docker", "compose", "ps"])
    for service in DIAGNOSTIC_SERVICES:
        print_command_output(
            f"docker compose logs --tail=80 {service}",
            ["docker", "compose", "logs", "--tail=80", service],
        )


def http_request(
    method: str,
    url: str,
    *,
    json_body: dict[str, Any] | None = None,
    timeout: int = 10,
) -> tuple[int, Any]:
    data = None
    headers = {"Accept": "application/json"}
    if json_body is not None:
        data = json.dumps(json_body).encode("utf-8")
        headers["Content-Type"] = "application/json"

    request = urllib.request.Request(
        url,
        data=data,
        headers=headers,
        method=method.upper(),
    )

    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            status_code = response.status
            body_text = response.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        status_code = exc.code
        body_text = exc.read().decode("utf-8", errors="replace")
    except (urllib.error.URLError, http.client.HTTPException, OSError, TimeoutError) as exc:
        raise SagaTestError(f"{method.upper()} {url} failed: {exc}") from exc

    if not body_text:
        return status_code, None

    try:
        return status_code, json.loads(body_text)
    except json.JSONDecodeError:
        return status_code, body_text


def request_json(
    method: str,
    url: str,
    *,
    expected_statuses: tuple[int, ...] = (200,),
    json_body: dict[str, Any] | None = None,
    **kwargs: Any,
) -> Any:
    if kwargs:
        raise TypeError(f"Unsupported request options: {sorted(kwargs)}")
    status_code, body = http_request(method, url, json_body=json_body)
    if status_code not in expected_statuses:
        pretty_body = json.dumps(body, indent=2, ensure_ascii=False) if isinstance(body, (dict, list)) else body
        raise SagaTestError(
            f"{method.upper()} {url} returned {status_code}, expected {expected_statuses}.\n"
            f"Response body:\n{pretty_body}"
        )
    return body


def wait_for_service(
    base_url: str,
    name: str,
    *,
    timeout: int = 180,
    health_path: str = "/actuator/health",
    ready_statuses: tuple[str, ...] = ("UP",),
) -> None:
    uri = f"{base_url}{health_path}"
    deadline = time.time() + timeout
    expected = {status.upper() for status in ready_statuses}
    last_error = ""

    print(f"[*] Waiting for {name}: {uri}")
    while time.time() < deadline:
        try:
            status_code, body = http_request("get", uri, timeout=3)
            if status_code == 200 and isinstance(body, dict):
                status = str(body.get("status", "")).upper()
                if status in expected:
                    print(f"    -> {name} is ready ({status})")
                    return
                last_error = f"status={status or body}"
            else:
                last_error = f"HTTP {status_code}: {body}"
        except SagaTestError as exc:
            last_error = str(exc)
        time.sleep(2)

    raise SagaTestError(f"Timed out waiting for {name}. Last result: {last_error}")


def wait_for_stack() -> None:
    wait_for_service(GATEWAY_URL, "api-gateway")
    wait_for_service(USER_URL, "user-service")
    wait_for_service(LISTING_URL, "listing-service")
    wait_for_service(TRADE_URL, "trade-service")
    wait_for_service(BILLING_URL, "billing-service", health_path="/health", ready_statuses=("ok",))


def seed_billing_accounts(accounts: dict[int, Decimal]) -> None:
    values = ", ".join(
        f"({user_id}, {balance}, 'EUR', 0.00, 'EUR', NOW())"
        for user_id, balance in accounts.items()
    )
    sql = f"""
INSERT INTO accounts (
    user_id, balance_amount, balance_currency, reserved_amount, reserved_currency, created_at
)
VALUES {values}
ON CONFLICT (user_id) DO UPDATE SET
    balance_amount = EXCLUDED.balance_amount,
    balance_currency = EXCLUDED.balance_currency,
    reserved_amount = 0.00,
    reserved_currency = 'EUR';
"""
    docker_compose(
        "exec",
        "-T",
        "billing-db",
        "psql",
        "-U",
        "postgres",
        "-d",
        "billing_db",
        "-v",
        "ON_ERROR_STOP=1",
        "-c",
        sql,
        capture=True,
    )


def new_user(name: str, email: str, role: str) -> dict[str, Any]:
    payload = {
        "name": name,
        "email": email,
        "rawPassword": "SecurePassword123!",
        "role": role,
    }
    user = request_json("post", f"{GATEWAY_URL}/users/register", json_body=payload)
    print(f"    user {role.lower():8s}: id={user['id']} email={email}")
    return user


def new_listing(
    seller_id: int,
    title: str,
    price: Decimal,
    capacity: float = 10.0,
) -> dict[str, Any]:
    payload = {
        "sellerId": seller_id,
        "title": title,
        "description": f"Integration saga asset: {title}",
        "priceAmount": float(price),
        "priceCurrency": "EUR",
        "capacityValue": capacity,
        "capacityUnit": "KWH",
    }
    listing = request_json("post", f"{GATEWAY_URL}/listings", expected_statuses=(201,), json_body=payload)
    print(f"    listing: id={listing['id']} status={listing['status']}")
    return listing


def new_trade(buyer_id: int, seller_id: int, listing_id: int, amount: Decimal) -> dict[str, Any]:
    payload = {
        "buyerId": buyer_id,
        "sellerId": seller_id,
        "listingId": listing_id,
        "amount": float(amount),
        "currency": "EUR",
    }
    trade = request_json("post", f"{GATEWAY_URL}/trades", json_body=payload)
    print(f"    trade: id={trade['tradeId']} initial_status={trade['status']}")
    return trade


def get_trade(trade_id: int) -> dict[str, Any]:
    return request_json("get", f"{GATEWAY_URL}/trades/{trade_id}")


def get_listing(listing_id: int) -> dict[str, Any]:
    return request_json("get", f"{GATEWAY_URL}/listings/{listing_id}")


def get_billing_account(user_id: int) -> dict[str, Any]:
    return request_json("get", f"{BILLING_URL}/billing/accounts/{user_id}")


def get_trade_receipt(trade_id: int) -> dict[str, Any]:
    return request_json("get", f"{GATEWAY_URL}/trades/{trade_id}/receipt")


def wait_for_trade_status(
    trade_id: int,
    expected_statuses: set[str],
    *,
    timeout: int = 90,
) -> dict[str, Any]:
    deadline = time.time() + timeout
    last_trade: dict[str, Any] | None = None
    last_status = None

    while time.time() < deadline:
        trade = get_trade(trade_id)
        last_trade = trade
        status = trade.get("status")
        if status != last_status:
            print(f"    trade {trade_id}: {status}")
            last_status = status
        if status in expected_statuses:
            return trade
        if status in TERMINAL_TRADE_STATUSES:
            raise SagaTestError(
                f"Trade {trade_id} reached terminal status {status}, expected {sorted(expected_statuses)}"
            )
        time.sleep(1)

    raise SagaTestError(
        f"Timed out waiting for trade {trade_id} to reach {sorted(expected_statuses)}. "
        f"Last trade: {last_trade}"
    )


def assert_equal(actual: Any, expected: Any, message: str) -> None:
    if actual != expected:
        raise SagaTestError(f"{message}: expected {expected!r}, got {actual!r}")


def assert_decimal(actual: Any, expected: Decimal, message: str) -> None:
    actual_decimal = Decimal(str(actual))
    if actual_decimal != expected:
        raise SagaTestError(f"{message}: expected {expected}, got {actual_decimal}")


def prepare_test_users() -> tuple[int, int, int]:
    run_id = f"{int(time.time())}-{uuid.uuid4().hex[:8]}"
    print("\n=== Preparing users ===")
    rich_buyer = new_user("Saga Rich Buyer", f"saga-rich-buyer-{run_id}@example.com", "CONSUMER")
    broke_buyer = new_user("Saga Broke Buyer", f"saga-broke-buyer-{run_id}@example.com", "CONSUMER")
    seller = new_user("Saga Seller", f"saga-seller-{run_id}@example.com", "PROSUMER")

    rich_buyer_id = int(rich_buyer["id"])
    broke_buyer_id = int(broke_buyer["id"])
    seller_id = int(seller["id"])

    print("\n=== Seeding billing accounts ===")
    seed_billing_accounts(
        {
            rich_buyer_id: Decimal("1000.00"),
            broke_buyer_id: Decimal("0.00"),
            seller_id: Decimal("0.00"),
        }
    )
    for user_id in (rich_buyer_id, broke_buyer_id, seller_id):
        account = get_billing_account(user_id)
        print(
            "    account: user_id={user_id} balance={balance} reserved={reserved}".format(
                user_id=user_id,
                balance=account["balance"]["amount"],
                reserved=account["reserved"]["amount"],
            )
        )

    return rich_buyer_id, broke_buyer_id, seller_id


def run_success_scenario(buyer_id: int, seller_id: int) -> None:
    print("\n=== Scenario: successful end-to-end saga ===")
    amount = Decimal("100.00")
    listing = new_listing(seller_id, "Saga Success Wind Pack", amount, capacity=85.0)
    trade = new_trade(buyer_id, seller_id, int(listing["id"]), amount)

    completed_trade = wait_for_trade_status(int(trade["tradeId"]), {"COMPLETED"})
    completed_listing = get_listing(int(listing["id"]))
    receipt = get_trade_receipt(int(trade["tradeId"]))
    buyer_account = get_billing_account(buyer_id)
    seller_account = get_billing_account(seller_id)

    assert_equal(completed_trade["status"], "COMPLETED", "trade status")
    assert_equal(completed_listing["status"], "COMPLETED", "listing status")
    assert_equal(receipt["tradeId"], completed_trade["tradeId"], "receipt trade id")
    assert_decimal(buyer_account["reserved"]["amount"], Decimal("0.00"), "buyer reserved funds")
    assert_decimal(buyer_account["balance"]["amount"], Decimal("900.00"), "buyer balance")
    assert_decimal(seller_account["balance"]["amount"], Decimal("100.00"), "seller balance")

    print("    OK: trade completed, listing completed, receipt exists, funds settled")


def run_compensation_scenario(buyer_id: int, seller_id: int) -> None:
    print("\n=== Scenario: payment authorization fails and listing is released ===")
    amount = Decimal("100.00")
    listing = new_listing(seller_id, "Saga Compensation Solar Pack", amount, capacity=45.5)
    trade = new_trade(buyer_id, seller_id, int(listing["id"]), amount)

    failed_trade = wait_for_trade_status(int(trade["tradeId"]), {"FAILED"})
    released_listing = get_listing(int(listing["id"]))
    buyer_account = get_billing_account(buyer_id)

    assert_equal(failed_trade["status"], "FAILED", "trade status after compensation")
    assert_equal(released_listing["status"], "AVAILABLE", "listing status after compensation")
    assert_decimal(buyer_account["balance"]["amount"], Decimal("0.00"), "broke buyer balance")
    assert_decimal(buyer_account["reserved"]["amount"], Decimal("0.00"), "broke buyer reserved funds")

    print("    OK: payment failed, trade failed, listing returned to AVAILABLE")


def run_reservation_failure_scenario(buyer_id: int, seller_id: int) -> None:
    print("\n=== Scenario: listing reservation fails for missing listing ===")
    missing_listing_id = 999_999_999_999
    trade = new_trade(buyer_id, seller_id, missing_listing_id, Decimal("10.00"))
    failed_trade = wait_for_trade_status(int(trade["tradeId"]), {"FAILED"})

    assert_equal(failed_trade["status"], "FAILED", "trade status for missing listing")
    print("    OK: missing listing produced FAILED trade")


def boot_stack(args: argparse.Namespace) -> None:
    if args.reset:
        print("[*] Resetting Docker Compose stack and named volumes")
        docker_compose("down", "-v", capture=True)

    print("[*] Starting Docker Compose stack")
    compose_args = ["up", "-d"]
    if not args.skip_build:
        compose_args.append("--build")
    docker_compose(*compose_args, capture=True)

    print("[*] Waiting for service health checks")
    wait_for_stack()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="End-to-end Saga integration test")
    parser.add_argument("--reset", action="store_true", help="Run docker compose down -v before booting")
    parser.add_argument("--skip-build", action="store_true", help="Do not rebuild service images")
    parser.add_argument(
        "--no-diagnostics",
        action="store_true",
        help="Do not print docker compose ps/logs on failure",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        boot_stack(args)
        rich_buyer_id, broke_buyer_id, seller_id = prepare_test_users()
        run_success_scenario(rich_buyer_id, seller_id)
        run_compensation_scenario(broke_buyer_id, seller_id)
        run_reservation_failure_scenario(rich_buyer_id, seller_id)
    except Exception as exc:
        print(f"\n[-] Saga test failed: {exc}", file=sys.stderr)
        if not args.no_diagnostics:
            print_diagnostics()
        return 1

    print("\n[+] All saga integration scenarios passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
