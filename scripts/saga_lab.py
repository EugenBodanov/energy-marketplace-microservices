from __future__ import annotations

import argparse
import json
import mimetypes
import os
import queue
import shutil
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request
import uuid
import webbrowser
from dataclasses import dataclass, field
from datetime import datetime, timezone
from decimal import Decimal
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any


PROJECT_ROOT = Path(__file__).resolve().parents[1]
UI_ROOT = PROJECT_ROOT / "saga-lab"

GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")
USER_URL = os.getenv("USER_URL", "http://localhost:8085")
LISTING_URL = os.getenv("LISTING_URL", "http://localhost:8082")
TRADE_URL = os.getenv("TRADE_URL", "http://localhost:8081")
BILLING_URL = os.getenv("BILLING_URL", "http://localhost:8083")

TERMINAL_TRADE_STATUSES = {"COMPLETED", "FAILED", "COMPENSATION_FAILED"}

SCENARIOS = {
    "happy-path": {
        "title": "Happy Path",
        "amount": Decimal("100.00"),
        "buyer_balance": Decimal("1000.00"),
        "missing_listing": False,
    },
    "reservation-failure": {
        "title": "Reservation Failure",
        "amount": Decimal("10.00"),
        "buyer_balance": Decimal("1000.00"),
        "missing_listing": True,
    },
    "payment-failure": {
        "title": "Payment Failure",
        "amount": Decimal("100.00"),
        "buyer_balance": Decimal("0.00"),
        "missing_listing": False,
    },
}

STATUS_TO_PHASE = {
    "CREATED": "Created",
    "USER_VALIDATION_PENDING": "Validate Users",
    "USER_VALIDATED": "Validate Users",
    "LISTING_RESERVATION_PENDING": "Reserve Listing",
    "LISTING_RESERVED": "Reserve Listing",
    "PAYMENT_AUTHORIZATION_PENDING": "Authorize Payment",
    "PAYMENT_AUTHORIZED": "Authorize Payment",
    "LISTING_COMPENSATION_PENDING": "Compensate Listing",
    "LISTING_CLOSING_PENDING": "Close Listing",
    "LISTING_CLOSED": "Close Listing",
    "PAYMENT_SETTLEMENT_PENDING": "Settle Payment",
    "PAYMENT_SETTLED": "Settle Payment",
    "COMPLETED_RECEIPT_PENDING": "Generate Receipt",
    "RECEIPT_GENERATION_PENDING": "Generate Receipt",
    "COMPLETED": "Completed",
    "FAILED": "Failed",
    "COMPENSATION_FAILED": "Compensation Failed",
}

SCENARIO_PHASES = {
    "happy-path": [
        "Created",
        "Validate Users",
        "Reserve Listing",
        "Authorize Payment",
        "Close Listing",
        "Settle Payment",
        "Generate Receipt",
        "Completed",
    ],
    "reservation-failure": ["Created", "Validate Users", "Reserve Listing", "Failed"],
    "payment-failure": [
        "Created",
        "Validate Users",
        "Reserve Listing",
        "Authorize Payment",
        "Compensate Listing",
        "Failed",
    ],
}


@dataclass
class LabState:
    running: bool = False
    completedPhases: list[str] = field(default_factory=list)
    failedPhases: list[str] = field(default_factory=list)
    currentOperation: str | None = None
    activeScenario: str | None = None
    activePhase: str | None = None
    phaseMode: str = "idle"
    tradeStatus: str | None = None
    phaseStatusDetails: dict[str, str] = field(default_factory=dict)
    snapshots: list[dict[str, Any]] = field(default_factory=list)
    tradeId: int | None = None
    listingId: int | None = None
    buyer: dict[str, Any] = field(default_factory=dict)
    seller: dict[str, Any] = field(default_factory=dict)
    receipt: dict[str, Any] = field(default_factory=dict)
    listing: dict[str, Any] = field(default_factory=dict)
    containers: list[dict[str, Any]] = field(default_factory=list)


class EventBus:
    def __init__(self) -> None:
        self._next_id = 1
        self._clients: list[queue.Queue[dict[str, Any]]] = []
        self._lock = threading.Lock()

    def emit(
        self,
        source: str,
        message: str,
        *,
        level: str = "info",
        details: dict[str, Any] | None = None,
    ) -> None:
        with self._lock:
            event = {
                "id": self._next_id,
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "level": level,
                "source": source,
                "message": message,
                "details": details or {},
            }
            self._next_id += 1
            clients = list(self._clients)

        print(
            f"[{event['timestamp']}] [{level.upper():7s}] [{source}] {message}",
            flush=True,
        )

        for client in clients:
            try:
                client.put_nowait(event)
            except queue.Full:
                pass

    def subscribe(self) -> queue.Queue[dict[str, Any]]:
        client: queue.Queue[dict[str, Any]] = queue.Queue(maxsize=500)
        with self._lock:
            self._clients.append(client)
        return client

    def unsubscribe(self, client: queue.Queue[dict[str, Any]]) -> None:
        with self._lock:
            if client in self._clients:
                self._clients.remove(client)


class CommandError(RuntimeError):
    def __init__(self, command: list[str], returncode: int, output: str) -> None:
        self.command = command
        self.returncode = returncode
        self.output = output
        super().__init__(f"{' '.join(command)} failed with exit code {returncode}")


class DockerController:
    def __init__(self, bus: EventBus) -> None:
        self.bus = bus

    def ensure_available(self) -> None:
        if shutil.which("docker") is None:
            raise RuntimeError("Docker is not installed or is not available on PATH.")
        self.run(["docker", "compose", "version"], timeout=30)

    def run(self, command: list[str], *, timeout: int = 300) -> subprocess.CompletedProcess[str]:
        self.bus.emit("docker", f"Running: {' '.join(command)}")
        try:
            result = subprocess.run(
                command,
                cwd=PROJECT_ROOT,
                text=True,
                capture_output=True,
                timeout=timeout,
                check=False,
            )
        except FileNotFoundError as exc:
            raise RuntimeError(f"Command not found: {command[0]}") from exc
        except subprocess.TimeoutExpired as exc:
            raise RuntimeError(f"Command timed out after {timeout}s: {' '.join(command)}") from exc

        output = ((result.stdout or "") + (result.stderr or "")).strip()
        if result.returncode != 0:
            if output:
                self.bus.emit("docker", output[-3000:], level="error")
            raise CommandError(command, result.returncode, output)
        if output:
            self.bus.emit("docker", output[-1200:], level="debug")
        return result

    def start_stack(self) -> None:
        self.ensure_available()
        self.run(["docker", "compose", "up", "-d", "--build"], timeout=900)
        self.bus.emit("docker", "Docker Compose stack started.", level="success")

    def reset_stack(self) -> None:
        self.ensure_available()
        self.run(["docker", "compose", "down", "-v", "--remove-orphans"], timeout=300)
        self.bus.emit("docker", "Volumes removed. Starting a clean stack.", level="warn")
        self.start_stack()

    def container_status(self) -> list[dict[str, Any]]:
        if shutil.which("docker") is None:
            return []
        try:
            result = subprocess.run(
                ["docker", "compose", "ps", "--format", "json"],
                cwd=PROJECT_ROOT,
                text=True,
                capture_output=True,
                timeout=20,
                check=False,
            )
        except (OSError, subprocess.TimeoutExpired):
            return []
        if result.returncode != 0 or not result.stdout.strip():
            return []

        raw = result.stdout.strip()
        try:
            parsed = json.loads(raw)
            items = parsed if isinstance(parsed, list) else [parsed]
        except json.JSONDecodeError:
            items = [json.loads(line) for line in raw.splitlines() if line.strip()]

        containers = []
        for item in items:
            containers.append(
                {
                    "service": item.get("Service") or item.get("Name") or "unknown",
                    "container": item.get("Name") or "",
                    "state": str(item.get("State") or "not created").lower(),
                    "health": str(item.get("Health") or "none").lower(),
                    "status": item.get("Status") or "",
                }
            )
        return sorted(containers, key=lambda row: row["service"])


class ApiClient:
    def __init__(self, bus: EventBus) -> None:
        self.bus = bus

    def request(
        self,
        method: str,
        url: str,
        *,
        json_body: dict[str, Any] | None = None,
        expected: tuple[int, ...] = (200,),
        timeout: int = 10,
    ) -> Any:
        data = None
        headers = {"Accept": "application/json"}
        if json_body is not None:
            data = json.dumps(json_body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        request = urllib.request.Request(url, data=data, headers=headers, method=method.upper())

        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                status = response.status
                body_text = response.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            status = exc.code
            body_text = exc.read().decode("utf-8", errors="replace")
        except (urllib.error.URLError, OSError, TimeoutError) as exc:
            raise RuntimeError(f"{method.upper()} {url} failed: {exc}") from exc

        body = None
        if body_text:
            try:
                body = json.loads(body_text)
            except json.JSONDecodeError:
                body = body_text

        if status not in expected:
            raise RuntimeError(
                f"{method.upper()} {url} returned HTTP {status}; expected {expected}. Body: {body}"
            )
        return body

    def wait_for_service(self, base_url: str, name: str, *, path: str, ok_values: set[str]) -> None:
        deadline = time.time() + 180
        last_error = ""
        self.bus.emit("health", f"Waiting for {name}: {base_url}{path}")
        while time.time() < deadline:
            try:
                body = self.request("GET", f"{base_url}{path}", timeout=3)
                status = str(body.get("status", "") if isinstance(body, dict) else body).upper()
                if status in ok_values:
                    self.bus.emit("health", f"{name} healthy ({status})", level="success")
                    return
                last_error = f"status={status or body}"
            except RuntimeError as exc:
                last_error = str(exc)
            time.sleep(2)
        raise RuntimeError(f"Timed out waiting for {name}. Last result: {last_error}")


class ScenarioRunner:
    def __init__(
        self,
        state: LabState,
        state_lock: threading.Lock,
        bus: EventBus,
        docker: DockerController,
        api: ApiClient,
    ) -> None:
        self.state = state
        self.state_lock = state_lock
        self.bus = bus
        self.docker = docker
        self.api = api

    def begin_phase(self, phase: str) -> None:
        with self.state_lock:
            self.state.activePhase = phase
            self.state.phaseMode = "running"
        self.bus.emit("scenario", f"Phase: {phase}")

    def complete_phase(self, phase: str, *, failed: bool = False) -> None:
        with self.state_lock:
            if failed:
                if phase not in self.state.failedPhases:
                    self.state.failedPhases.append(phase)
                if phase in self.state.completedPhases:
                    self.state.completedPhases.remove(phase)
            elif phase not in self.state.completedPhases:
                self.state.completedPhases.append(phase)
                if phase in self.state.failedPhases:
                    self.state.failedPhases.remove(phase)
            self.state.phaseMode = "idle"
        time.sleep(0.45)

    def run_phase(self, phase: str, action: Any | None = None, *, failed: bool = False) -> Any:
        self.begin_phase(phase)
        result = action() if action else None
        self.complete_phase(phase, failed=failed)
        return result

    def run(self, scenario_id: str) -> None:
        scenario = SCENARIOS[scenario_id]
        scenario_phases = SCENARIO_PHASES[scenario_id]
        with self.state_lock:
            self.state.running = True
            self.state.completedPhases = []
            self.state.failedPhases = []
            self.state.snapshots = []
            self.state.activeScenario = scenario_id
            self.state.activePhase = None
            self.state.phaseMode = "idle"
            self.state.tradeStatus = None
            self.state.phaseStatusDetails = {}
            self.state.tradeId = None
            self.state.listingId = None
            self.state.buyer = {}
            self.state.seller = {}
            self.state.receipt = {}
            self.state.listing = {}

        try:
            self.bus.emit("scenario", f"Starting scenario: {scenario['title']}", level="success")
            self.run_phase("Created")
            self.capture_snapshot("Created")

            def validate_and_prepare() -> tuple[dict[str, Any], dict[str, Any]]:
                self.wait_for_stack()
                return self.prepare_users(
                    buyer_balance=scenario["buyer_balance"],
                    scenario_id=scenario_id,
                )

            buyer, seller = self.run_phase(
                "Validate Users",
                validate_and_prepare,
            )
            self.capture_snapshot("Validate Users")

            listing_id = 999_999_999_999
            if scenario["missing_listing"]:
                self.bus.emit("scenario", f"Using missing listing id={listing_id}", level="warn")
            else:
                listing = self.new_listing(int(seller["id"]), scenario["title"], scenario["amount"])
                listing_id = int(listing["id"])
                with self.state_lock:
                    self.state.listingId = listing_id

            self.begin_phase("Reserve Listing")
            trade = self.new_trade(int(buyer["id"]), int(seller["id"]), listing_id, scenario["amount"])
            trade_id = int(trade["tradeId"])
            with self.state_lock:
                self.state.tradeId = trade_id
                self.state.listingId = listing_id

            self.refresh_balances(int(buyer["id"]), int(seller["id"]))
            self.refresh_listing(listing_id)
            self.capture_snapshot("Reserve Listing")

            final_trade = self.wait_for_terminal_trade(
                trade_id,
                int(buyer["id"]),
                int(seller["id"]),
                listing_id,
                scenario_id,
                scenario_phases,
            )
            self.refresh_balances(int(buyer["id"]), int(seller["id"]))
            self.refresh_listing(listing_id)

            final_status = str(final_trade.get("status"))
            if final_status == "COMPLETED":
                self.fetch_receipt_if_available(trade_id)
                self.capture_snapshot("Generate Receipt")
                self.capture_snapshot("Completed")
            elif scenario_id == "reservation-failure":
                self.complete_phase("Reserve Listing", failed=True)
                self.capture_snapshot("Reserve Listing")
                self.capture_snapshot("Failed")
            elif scenario_id == "payment-failure":
                self.complete_phase("Authorize Payment", failed=True)
                self.complete_phase("Compensate Listing")
                self.capture_snapshot("Authorize Payment")
                self.capture_snapshot("Compensate Listing")
                self.capture_snapshot("Failed")

            self.bus.emit("scenario", f"Scenario finished with trade status {final_trade['status']}.", level="success")
        except Exception as exc:
            self.bus.emit("scenario", f"Scenario failed: {exc}", level="error")
        finally:
            with self.state_lock:
                self.state.running = False
                self.state.phaseMode = "idle"

    def wait_for_stack(self) -> None:
        self.api.wait_for_service(GATEWAY_URL, "api-gateway", path="/actuator/health", ok_values={"UP"})
        self.api.wait_for_service(USER_URL, "user-service", path="/actuator/health", ok_values={"UP"})
        self.api.wait_for_service(LISTING_URL, "listing-service", path="/actuator/health", ok_values={"UP"})
        self.api.wait_for_service(TRADE_URL, "trade-service", path="/actuator/health", ok_values={"UP"})
        self.api.wait_for_service(BILLING_URL, "billing-service", path="/health", ok_values={"OK"})

    def prepare_users(self, *, buyer_balance: Decimal, scenario_id: str) -> tuple[dict[str, Any], dict[str, Any]]:
        run_id = f"{int(time.time())}-{uuid.uuid4().hex[:8]}"
        buyer = self.new_user(f"Saga Lab Buyer {scenario_id}", f"saga-lab-buyer-{run_id}@example.com", "CONSUMER")
        seller = self.new_user(f"Saga Lab Seller {scenario_id}", f"saga-lab-seller-{run_id}@example.com", "PROSUMER")
        self.seed_billing_accounts({int(buyer["id"]): buyer_balance, int(seller["id"]): Decimal("0.00")})
        return buyer, seller

    def new_user(self, name: str, email: str, role: str) -> dict[str, Any]:
        payload = {
            "name": name,
            "email": email,
            "rawPassword": "SecurePassword123!",
            "role": role,
        }
        user = self.api.request("POST", f"{GATEWAY_URL}/users/register", json_body=payload)
        self.bus.emit("api", f"Created {role.lower()} user id={user['id']} email={email}")
        return user

    def seed_billing_accounts(self, accounts: dict[int, Decimal]) -> None:
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
        self.docker.run(
            [
                "docker",
                "compose",
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
            ],
            timeout=60,
        )
        self.bus.emit("billing", "Seeded buyer and seller billing accounts.", level="success")

    def new_listing(self, seller_id: int, title: str, amount: Decimal) -> dict[str, Any]:
        payload = {
            "sellerId": seller_id,
            "title": f"Saga Lab {title}",
            "description": f"Saga Lab asset for {title}",
            "priceAmount": float(amount),
            "priceCurrency": "EUR",
            "capacityValue": 50.0,
            "capacityUnit": "KWH",
        }
        listing = self.api.request("POST", f"{GATEWAY_URL}/listings", json_body=payload, expected=(201,))
        self.bus.emit("api", f"Created listing id={listing['id']} status={listing['status']}")
        return listing

    def new_trade(self, buyer_id: int, seller_id: int, listing_id: int, amount: Decimal) -> dict[str, Any]:
        payload = {
            "buyerId": buyer_id,
            "sellerId": seller_id,
            "listingId": listing_id,
            "amount": float(amount),
            "currency": "EUR",
        }
        trade = self.api.request("POST", f"{GATEWAY_URL}/trades", json_body=payload)
        self.bus.emit("api", f"Created trade id={trade['tradeId']} initial_status={trade['status']}")
        return trade

    def wait_until_phase_advances(
        self,
        trade_id: int,
        target_phase: str,
        scenario_phases: list[str],
    ) -> dict[str, Any]:
        target_index = scenario_phases.index(target_phase)
        deadline = time.time() + 90
        last_status = None
        last_trade: dict[str, Any] | None = None
        while time.time() < deadline:
            trade = self.api.request("GET", f"{GATEWAY_URL}/trades/{trade_id}")
            last_trade = trade
            status = trade.get("status")
            if status != last_status:
                phase = STATUS_TO_PHASE.get(status, status)
                self.sync_from_trade_status(str(status), str(phase), scenario_phases)
                self.bus.emit("trade", f"Trade {trade_id} -> {status}")
                last_status = status

            current_phase = STATUS_TO_PHASE.get(status, str(status))
            current_index = scenario_phases.index(current_phase) if current_phase in scenario_phases else -1
            if current_index > target_index:
                return trade
            if status in TERMINAL_TRADE_STATUSES and current_index >= target_index:
                return trade
            if status in TERMINAL_TRADE_STATUSES:
                raise RuntimeError(
                    f"Trade {trade_id} reached terminal status {status} before completing {target_phase}"
                )
            time.sleep(1)
        raise RuntimeError(
            f"Timed out waiting for trade {trade_id} to advance past {target_phase}. Last trade: {last_trade}"
        )

    def wait_for_terminal_trade(
        self,
        trade_id: int,
        buyer_id: int,
        seller_id: int,
        listing_id: int,
        scenario_id: str,
        scenario_phases: list[str],
    ) -> dict[str, Any]:
        deadline = time.time() + 120
        last_status = None
        last_trade: dict[str, Any] | None = None
        last_phase: str | None = None

        while time.time() < deadline:
            trade = self.api.request("GET", f"{GATEWAY_URL}/trades/{trade_id}")
            last_trade = trade
            status = str(trade.get("status"))
            phase = STATUS_TO_PHASE.get(status, status)

            self.sync_from_trade_status(status, phase, scenario_phases)

            if status != last_status:
                self.bus.emit("trade", f"Trade {trade_id} -> {status}")
                if status == "PAYMENT_AUTHORIZED":
                    self.bus.emit("billing", "Payment authorized. Buyer funds are now reserved.", level="success")
                if status == "LISTING_COMPENSATION_PENDING" and scenario_id == "payment-failure":
                    self.bus.emit("billing", "Payment authorization failed because buyer funds were insufficient.", level="warn")
                last_status = status

            if phase != last_phase and phase in scenario_phases:
                self.refresh_balances(buyer_id, seller_id)
                self.refresh_listing(listing_id)
                self.capture_snapshot(phase)
                last_phase = phase

            if status in TERMINAL_TRADE_STATUSES:
                self.refresh_balances(buyer_id, seller_id)
                self.refresh_listing(listing_id)
                if status == "COMPLETED":
                    self.fetch_receipt_if_available(trade_id, quiet=True)
                terminal_phase = STATUS_TO_PHASE.get(status, status)
                self.capture_snapshot(terminal_phase)
                return trade

            time.sleep(1)

        raise RuntimeError(
            f"Timed out waiting for trade {trade_id} to reach a terminal status. Last trade: {last_trade}"
        )

    def fetch_receipt_if_available(self, trade_id: int, *, quiet: bool = False) -> dict[str, Any] | None:
        try:
            receipt = self.api.request("GET", f"{GATEWAY_URL}/trades/{trade_id}/receipt", expected=(200,))
        except Exception as exc:
            if not quiet:
                self.bus.emit("billing", f"Receipt is not available yet: {exc}", level="warn")
            return None

        with self.state_lock:
            self.state.receipt = receipt
        if not quiet:
            self.bus.emit(
                "scenario",
                f"Receipt #{receipt['receiptId']} is available for trade {trade_id}.",
                level="success",
            )
        return receipt

    def sync_from_trade_status(self, status: str, phase: str, scenario_phases: list[str]) -> None:
        with self.state_lock:
            self.state.tradeStatus = status
            if phase:
                self.state.phaseStatusDetails[phase] = status

            if not self.state.running:
                return

            if status == "COMPLETED":
                self.state.activePhase = None
                self.state.phaseMode = "idle"
                for completed_phase in scenario_phases:
                    if completed_phase not in self.state.failedPhases and completed_phase not in self.state.completedPhases:
                        self.state.completedPhases.append(completed_phase)
                return

            if status in {"FAILED", "COMPENSATION_FAILED"}:
                self.state.activePhase = "Failed" if "Failed" in scenario_phases else "Compensation Failed"
                self.state.phaseMode = "idle"
                if self.state.activePhase not in self.state.failedPhases:
                    self.state.failedPhases.append(self.state.activePhase)
                return

            if phase not in scenario_phases:
                return

            current_index = scenario_phases.index(phase)
            for completed_phase in scenario_phases[:current_index]:
                if completed_phase not in self.state.failedPhases and completed_phase not in self.state.completedPhases:
                    self.state.completedPhases.append(completed_phase)

            self.state.activePhase = phase
            self.state.phaseMode = "running"

    def capture_snapshot(self, phase: str) -> None:
        if not phase:
            return
        with self.state_lock:
            existing_index = next(
                (index for index, snapshot in enumerate(self.state.snapshots) if snapshot.get("phase") == phase),
                None,
            )
            snapshot = {
                "phase": phase,
                "tradeStatus": self.state.tradeStatus,
                "capturedAt": datetime.now(timezone.utc).isoformat(),
                "buyer": dict(self.state.buyer) if self.state.buyer else {},
                "seller": dict(self.state.seller) if self.state.seller else {},
                "listing": dict(self.state.listing) if self.state.listing else {},
                "receipt": dict(self.state.receipt) if self.state.receipt else {},
            }
            if existing_index is None:
                self.state.snapshots.append(snapshot)
            else:
                self.state.snapshots[existing_index] = snapshot

    def refresh_balances(self, buyer_id: int, seller_id: int) -> None:
        buyer = self.account_snapshot(buyer_id)
        seller = self.account_snapshot(seller_id)
        with self.state_lock:
            self.state.buyer = buyer
            self.state.seller = seller
        self.bus.emit(
            "billing",
            "Balances updated: buyer balance={buyer_balance} reserved={buyer_reserved}; "
            "seller balance={seller_balance} reserved={seller_reserved}".format(
                buyer_balance=buyer.get("balance"),
                buyer_reserved=buyer.get("reserved"),
                seller_balance=seller.get("balance"),
                seller_reserved=seller.get("reserved"),
            ),
        )

    def poll_balances(self, buyer_id: int, seller_id: int) -> None:
        buyer = self.account_snapshot(buyer_id)
        seller = self.account_snapshot(seller_id)
        with self.state_lock:
            self.state.buyer = buyer
            self.state.seller = seller

    def refresh_listing(self, listing_id: int) -> None:
        snapshot = self.listing_snapshot(listing_id)
        with self.state_lock:
            self.state.listing = snapshot
        self.bus.emit("api", f"Listing snapshot updated: id={listing_id} status={snapshot['status']}")

    def poll_listing(self, listing_id: int) -> None:
        snapshot = self.listing_snapshot(listing_id)
        with self.state_lock:
            self.state.listing = snapshot

    def listing_snapshot(self, listing_id: int) -> dict[str, Any]:
        try:
            listing = self.api.request("GET", f"{GATEWAY_URL}/listings/{listing_id}")
            return {
                "id": listing["id"],
                "sellerId": listing.get("sellerId"),
                "title": listing.get("title"),
                "priceAmount": listing.get("priceAmount"),
                "priceCurrency": listing.get("priceCurrency"),
                "capacityValue": listing.get("capacityValue"),
                "capacityUnit": listing.get("capacityUnit"),
                "status": listing.get("status", "UNKNOWN"),
                "reservationReference": listing.get("reservationReference"),
                "updatedAt": listing.get("updatedAt"),
            }
        except Exception as exc:
            return {
                "id": listing_id,
                "status": "NOT_FOUND",
                "error": str(exc),
            }

    def account_snapshot(self, user_id: int) -> dict[str, Any]:
        account = self.api.request("GET", f"{BILLING_URL}/billing/accounts/{user_id}")
        return {
            "userId": user_id,
            "balance": str(account["balance"]["amount"]),
            "reserved": str(account["reserved"]["amount"]),
            "currency": account["balance"].get("currency", "EUR"),
        }


class SagaLabApp:
    def __init__(self, port: int, ui_port: int, open_browser: bool) -> None:
        self.port = port
        self.ui_port = ui_port
        self.open_browser = open_browser
        self.bus = EventBus()
        self.state = LabState()
        self.state_lock = threading.Lock()
        self.docker = DockerController(self.bus)
        self.api = ApiClient(self.bus)
        self.runner = ScenarioRunner(self.state, self.state_lock, self.bus, self.docker, self.api)
        self._operation_lock = threading.Lock()
        self._vite_process: subprocess.Popen[str] | None = None
        self._ui_url = f"http://127.0.0.1:{self.port}"
        self._last_container_key = ""
        self._last_domain_key = ""

    def reset_run_state(self) -> None:
        with self.state_lock:
            self.state.running = False
            self.state.completedPhases = []
            self.state.failedPhases = []
            self.state.snapshots = []
            self.state.currentOperation = None
            self.state.activeScenario = None
            self.state.activePhase = None
            self.state.phaseMode = "idle"
            self.state.tradeStatus = None
            self.state.phaseStatusDetails = {}
            self.state.tradeId = None
            self.state.listingId = None
            self.state.buyer = {}
            self.state.seller = {}
            self.state.receipt = {}
            self.state.listing = {}
        self.bus.emit("ui", "Run state reset. Choose a scenario and start again.", level="success")

    def start(self) -> None:
        server = ThreadingHTTPServer(("127.0.0.1", self.port), self._handler_class())
        threading.Thread(target=self._poll_containers, daemon=True).start()
        threading.Thread(target=self._poll_domain_snapshots, daemon=True).start()
        for container in ("energy-trade-service", "energy-listing-service", "energy-billing-service"):
            threading.Thread(target=self._stream_container_logs, args=(container,), daemon=True).start()
        self._start_ui()
        self.bus.emit("ui", f"Saga Lab API running at http://127.0.0.1:{self.port}", level="success")
        if self.open_browser:
            webbrowser.open(self._ui_url)
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            self.bus.emit("ui", "Shutting down Saga Lab.", level="warn")
        finally:
            server.shutdown()
            if self._vite_process:
                self._vite_process.terminate()

    def _start_ui(self) -> None:
        if (UI_ROOT / "dist" / "index.html").exists():
            self._ui_url = f"http://127.0.0.1:{self.port}"
            self.bus.emit("ui", f"Serving built React UI at {self._ui_url}", level="success")
            return

        npm = shutil.which("npm.cmd") or shutil.which("npm")
        node_modules = UI_ROOT / "node_modules"
        if npm is None:
            self.bus.emit("ui", "npm was not found. Install Node.js/npm to run the React UI.", level="error")
            return
        if not node_modules.exists():
            self.bus.emit(
                "ui",
                "React dependencies are not installed. Run: cd saga-lab && npm install",
                level="warn",
            )
            return
        env = os.environ.copy()
        env["VITE_SAGA_LAB_API"] = f"http://127.0.0.1:{self.port}"
        self._ui_url = f"http://127.0.0.1:{self.ui_port}"
        self._vite_process = subprocess.Popen(
            [npm, "run", "dev", "--", "--port", str(self.ui_port)],
            cwd=UI_ROOT,
            env=env,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
        )
        threading.Thread(target=self._pipe_vite_output, daemon=True).start()
        self.bus.emit("ui", f"React UI starting at http://127.0.0.1:{self.ui_port}", level="success")

    def _pipe_vite_output(self) -> None:
        if not self._vite_process or not self._vite_process.stdout:
            return
        for line in self._vite_process.stdout:
            text = line.strip()
            if text:
                self.bus.emit("ui", text, level="debug")

    def _poll_containers(self) -> None:
        while True:
            containers = self.docker.container_status()
            with self.state_lock:
                self.state.containers = containers
            key = json.dumps(containers, sort_keys=True)
            if key and key != self._last_container_key:
                self._last_container_key = key
                for container in containers:
                    self.bus.emit(
                        "docker",
                        "{service}: state={state} health={health} status={status}".format(**container),
                        level="debug",
                    )
            time.sleep(3)

    def _stream_container_logs(self, container: str) -> None:
        if shutil.which("docker") is None:
            return
        command = ["docker", "logs", "--since", "0s", "-f", container]
        while True:
            try:
                process = subprocess.Popen(
                    command,
                    cwd=PROJECT_ROOT,
                    text=True,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT,
                )
            except OSError as exc:
                self.bus.emit("docker", f"Cannot stream {container} logs: {exc}", level="warn")
                time.sleep(5)
                continue

            if process.stdout:
                for line in process.stdout:
                    text = line.rstrip()
                    if not text:
                        continue
                    lower = text.lower()
                    is_access_log = " - \"" in text and "http/" in lower
                    if is_access_log and "/health" not in lower:
                        continue
                    level = "error" if " error " in lower or "exception" in lower else "debug"
                    self.bus.emit(container.replace("energy-", ""), text, level=level)

            process.wait()
            time.sleep(3)

    def _poll_domain_snapshots(self) -> None:
        while True:
            with self.state_lock:
                running = self.state.running
                scenario_id = self.state.activeScenario
                buyer_id = self.state.buyer.get("userId")
                seller_id = self.state.seller.get("userId")
                listing_id = self.state.listingId
                trade_id = self.state.tradeId

            if running:
                try:
                    if trade_id:
                        trade = self.api.request("GET", f"{GATEWAY_URL}/trades/{trade_id}", timeout=3)
                        status = str(trade.get("status"))
                        phase = STATUS_TO_PHASE.get(status, status)
                        if scenario_id in SCENARIO_PHASES:
                            self.runner.sync_from_trade_status(
                                status,
                                str(phase),
                                SCENARIO_PHASES[scenario_id],
                            )
                    if buyer_id and seller_id:
                        self.runner.poll_balances(int(buyer_id), int(seller_id))
                    if listing_id:
                        self.runner.poll_listing(int(listing_id))
                    with self.state_lock:
                        active_phase = self.state.activePhase
                        domain = {
                            "tradeStatus": self.state.tradeStatus,
                            "buyer": self.state.buyer,
                            "seller": self.state.seller,
                            "listing": self.state.listing,
                            "receipt": self.state.receipt,
                        }
                    if active_phase:
                        self.runner.capture_snapshot(active_phase)
                    domain_key = json.dumps(domain, sort_keys=True)
                    if domain_key != self._last_domain_key:
                        self._last_domain_key = domain_key
                        buyer = domain["buyer"] or {}
                        seller = domain["seller"] or {}
                        listing = domain["listing"] or {}
                        receipt = domain["receipt"] or {}
                        self.bus.emit(
                            "api",
                            (
                                f"DB snapshot: trade={domain['tradeStatus']} "
                                f"buyer(balance={buyer.get('balance')} reserved={buyer.get('reserved')}) "
                                f"seller(balance={seller.get('balance')} reserved={seller.get('reserved')}) "
                                f"listing(id={listing.get('id')} status={listing.get('status')}) "
                                f"receipt={receipt.get('receiptId')}"
                            ),
                            level="debug",
                        )
                except Exception:
                    pass

            time.sleep(1)

    def _run_exclusive(self, source: str, label: str, action: Any) -> None:
        if not self._operation_lock.acquire(blocking=False):
            self.bus.emit(source, f"Cannot start {label}; another operation is already running.", level="warn")
            return
        with self.state_lock:
            self.state.currentOperation = label
        try:
            action()
        except CommandError as exc:
            self.bus.emit(source, f"{label} failed: {exc}. {exc.output[-1600:]}", level="error")
        except Exception as exc:
            self.bus.emit(source, f"{label} failed: {exc}", level="error")
        finally:
            with self.state_lock:
                self.state.currentOperation = None
            self._operation_lock.release()

    def _handler_class(self) -> type[BaseHTTPRequestHandler]:
        app = self

        class Handler(BaseHTTPRequestHandler):
            protocol_version = "HTTP/1.1"

            def handle(self) -> None:
                try:
                    super().handle()
                except ConnectionResetError:
                    return

            def log_message(self, format: str, *args: Any) -> None:
                return

            def do_OPTIONS(self) -> None:
                self.send_response(204)
                self._cors()
                self.end_headers()

            def do_GET(self) -> None:
                if self.path == "/api/status":
                    self._json(app.status_payload())
                    return
                if self.path == "/api/events":
                    self._events()
                    return
                self._static()

            def do_POST(self) -> None:
                try:
                    if self.path == "/api/docker/start":
                        threading.Thread(
                            target=app._run_exclusive,
                            args=("docker", "start stack", app.docker.start_stack),
                            daemon=True,
                        ).start()
                        self._json({"ok": True})
                        return
                    if self.path == "/api/docker/reset":
                        threading.Thread(
                            target=app._run_exclusive,
                            args=("docker", "reset stack", app.docker.reset_stack),
                            daemon=True,
                        ).start()
                        self._json({"ok": True})
                        return
                    if self.path == "/api/lab/reset":
                        if app.state.running:
                            self._json({"error": "Cannot reset while a saga is running."}, status=409)
                            return
                        app.reset_run_state()
                        self._json({"ok": True})
                        return
                    if self.path == "/api/scenarios/start":
                        body = self._body()
                        scenario_id = body.get("scenarioId")
                        if scenario_id not in SCENARIOS:
                            self._json({"error": f"Unknown scenario: {scenario_id}"}, status=400)
                            return
                        threading.Thread(
                            target=app._run_exclusive,
                            args=(
                                "scenario",
                                f"scenario {scenario_id}",
                                lambda: app.runner.run(scenario_id),
                            ),
                            daemon=True,
                        ).start()
                        self._json({"ok": True})
                        return
                    self._json({"error": "Not found"}, status=404)
                except Exception as exc:
                    app.bus.emit("api", f"Request failed: {exc}", level="error")
                    self._json({"error": str(exc)}, status=500)

            def _body(self) -> dict[str, Any]:
                length = int(self.headers.get("Content-Length", "0"))
                if length == 0:
                    return {}
                return json.loads(self.rfile.read(length).decode("utf-8"))

            def _events(self) -> None:
                client = app.bus.subscribe()
                self.send_response(200)
                self._cors()
                self.send_header("Content-Type", "text/event-stream")
                self.send_header("Cache-Control", "no-cache")
                self.send_header("Connection", "keep-alive")
                self.end_headers()
                try:
                    while True:
                        try:
                            event = client.get(timeout=20)
                            payload = json.dumps(event)
                            self.wfile.write(f"data: {payload}\n\n".encode("utf-8"))
                        except queue.Empty:
                            self.wfile.write(b": keepalive\n\n")
                        self.wfile.flush()
                except (BrokenPipeError, ConnectionResetError):
                    pass
                finally:
                    app.bus.unsubscribe(client)

            def _json(self, payload: dict[str, Any], *, status: int = 200) -> None:
                data = json.dumps(payload).encode("utf-8")
                self.send_response(status)
                self._cors()
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(data)))
                self.end_headers()
                self.wfile.write(data)

            def _static(self) -> None:
                dist = UI_ROOT / "dist"
                if not (dist / "index.html").exists():
                    message = (
                        "<!doctype html><title>Saga Lab</title>"
                        "<main style='font-family:system-ui;padding:24px'>"
                        "<h1>Saga Lab API is running</h1>"
                        "<p>The built React UI was not found.</p>"
                        "<pre>cd saga-lab\nnpm install\nnpm run build\npython scripts/saga_lab.py</pre>"
                        "</main>"
                    ).encode("utf-8")
                    self.send_response(200)
                    self._cors()
                    self.send_header("Content-Type", "text/html; charset=utf-8")
                    self.send_header("Content-Length", str(len(message)))
                    self.end_headers()
                    self.wfile.write(message)
                    return

                clean_path = self.path.split("?", 1)[0].lstrip("/")
                target = (dist / clean_path).resolve() if clean_path else dist / "index.html"
                if not str(target).startswith(str(dist.resolve())) or not target.exists() or target.is_dir():
                    target = dist / "index.html"
                content = target.read_bytes()
                content_type = mimetypes.guess_type(str(target))[0] or "application/octet-stream"
                self.send_response(200)
                self._cors()
                self.send_header("Content-Type", content_type)
                self.send_header("Content-Length", str(len(content)))
                self.end_headers()
                self.wfile.write(content)

            def _cors(self) -> None:
                self.send_header("Access-Control-Allow-Origin", "*")
                self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                self.send_header("Access-Control-Allow-Headers", "Content-Type")

        return Handler

    def status_payload(self) -> dict[str, Any]:
        with self.state_lock:
            stack_ready = self._stack_ready(self.state.containers)
            return {
                "running": self.state.running,
                "completedPhases": self.state.completedPhases,
                "failedPhases": self.state.failedPhases,
                "currentOperation": self.state.currentOperation,
                "activeScenario": self.state.activeScenario,
                "activePhase": self.state.activePhase,
                "phaseMode": self.state.phaseMode,
                "tradeStatus": self.state.tradeStatus,
                "phaseStatusDetails": self.state.phaseStatusDetails,
                "snapshots": self.state.snapshots,
                "tradeId": self.state.tradeId,
                "listingId": self.state.listingId,
                "buyer": self.state.buyer,
                "seller": self.state.seller,
                "receipt": self.state.receipt,
                "listing": self.state.listing,
                "containers": self.state.containers,
                "stackReady": stack_ready,
            }

    def _stack_ready(self, containers: list[dict[str, Any]]) -> bool:
        required = {
            "api-gateway",
            "billing-service",
            "listing-service",
            "trade-service",
            "user-service",
            "rabbitmq",
        }
        by_service = {container["service"]: container for container in containers}
        for service in required:
            container = by_service.get(service)
            if not container:
                return False
            if container["state"] != "running":
                return False
            health = container.get("health", "none")
            if health not in {"none", "healthy"}:
                return False
        return True


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run the Energy Marketplace Saga Lab.")
    parser.add_argument("--port", type=int, default=8090, help="Python control API port.")
    parser.add_argument("--ui-port", type=int, default=5173, help="React/Vite UI port.")
    parser.add_argument("--no-browser", action="store_true", help="Do not open a browser automatically.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    app = SagaLabApp(port=args.port, ui_port=args.ui_port, open_browser=not args.no_browser)
    app.start()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
