import sys
import time
import subprocess
import argparse
import requests

# --- CORRECTED PORT ALLOCATIONS BASED ON APPLICATION.YAML ---
GATEWAY_URL = "http://localhost:8080"  # API Gateway Engine port
USER_URL    = "http://localhost:8081"  # User Service Core
LISTING_URL = "http://localhost:8082"  # Listing Service Core
TRADE_URL   = "http://localhost:8083"  # Trade Service Core

def run_command(command_list):
    """Executes a system shell command safely, dropping on errors."""
    try:
        subprocess.run(command_list, check=True)
    except subprocess.CalledProcessError as e:
        print(f"[-] Infrastructure modification command failed: {e}")
        sys.exit(1)

def wait_for_service(base_url, name, timeout=120):
    """Polls downstream actuators using clean, non-blocking HTTP pooling layers."""
    uri = f"{base_url}/actuator/health"
    deadline = time.time() + timeout
    print(f"[*] Vetting service health: {name} at {uri}")

    while time.time() < deadline:
        try:
            response = requests.get(uri, timeout=3)
            if response.status_code == 200:
                data = response.json()
                if data.get("status") == "UP":
                    print(f" -> {name} is READY and reporting UP!")
                    return True
        except (requests.exceptions.RequestException, ValueError):
            pass
        time.sleep(2)

    raise TimeoutError(f"Timeout waiting for service component to become fully ready (UP): {name}")

# --- API ROUTING INTERFACES (COMMUNICATING THROUGH APIGATEWAYAPPLICATION) ---

def new_user(name, email, role):
    payload = {
        "name": name,
        "email": email,
        "rawPassword": "SecurePassword123!",
        "role": role  # Correctly accepts "CONSUMER" and "PROSUMER" domain values
    }
    # Gateway Route: /users/** -> points to user-service-route
    res = requests.post(f"{GATEWAY_URL}/users/register", json=payload)
    res.raise_for_status()
    return res.json()

def new_listing(seller_id, title, description, price, currency, capacity_val, capacity_unit):
    payload = {
        "sellerId": seller_id,
        "title": title,
        "description": description,
        "priceAmount": price,
        "priceCurrency": currency,
        "capacityValue": capacity_val,
        "capacityUnit": capacity_unit
    }
    # Gateway Route: /listings/** -> points to listing-service-route
    res = requests.post(f"{GATEWAY_URL}/listings", json=payload)
    res.raise_for_status()
    return res.json()

def new_trade(buyer_id, seller_id, listing_id, amount):
    payload = {
        "buyerId": buyer_id,
        "sellerId": seller_id,
        "listingId": listing_id,
        "amount": amount,
        "currency": "EUR"
    }
    # Gateway Route: /trades/** -> points to trade-service-route
    res = requests.post(f"{GATEWAY_URL}/trades", json=payload)
    res.raise_for_status()
    return res.json()

def wait_for_trade_status(trade_id, expected_statuses, max_retries=15):
    for _ in range(max_retries):
        res = requests.get(f"{GATEWAY_URL}/trades/{trade_id}")
        res.raise_for_status()
        trade = res.json()
        if trade.get("status") in expected_statuses:
            return trade
        time.sleep(1)
    raise TimeoutError(f"Saga execution failed to settle on expected state. Current: {trade.get('status')}")

def assert_equal(actual, expected, message):
    if actual != expected:
        raise AssertionError(f"Verification Failed: {message}. Expected '{expected}', but encountered '{actual}'.")

# --- SCENARIO SUITES ---

def run_compensation_scenario(buyer_id, seller_id):
    print("\n=== STARTING SCENARIO: SAGA COMPENSATION (BUYER HAS NO FUNDS) ===")
    listing = new_listing(seller_id, "Saga Compensation Solar Pack", "High density storage configuration for rollbacks.", 100.00, "EUR", 45.5, "KWH")

    # Using the broke buyer profile to trigger validation failures inside accounting
    trade = new_trade(buyer_id, seller_id, listing["id"], 100.00)

    compensated_trade = wait_for_trade_status(trade["tradeId"], ["FAILED", "COMPENSATION_FAILED"])

    res_listing = requests.get(f"{GATEWAY_URL}/listings/{listing['id']}")
    compensated_listing = res_listing.json()

    print(f"Resulting Trade Status: {compensated_trade.get('status')}")
    print(f"Resulting Listing Status: {compensated_listing.get('status')}")

    assert_equal(compensated_trade.get("status"), "FAILED", "Trade status rollback verification")
    assert_equal(compensated_listing.get("status"), "AVAILABLE", "Listing inventory stock release verification")
    print("[+] Success: Saga compensation isolated funding failure and restored listing availability.")

def run_success_scenario(buyer_id, seller_id):
    print("\n=== STARTING SCENARIO: END-TO-END SAGA COMMIT (SUCCESS PATH) ===")
    listing = new_listing(seller_id, "Saga Success Wind Pack", "Premium grid infrastructure asset for happy path pipeline confirmation.", 100.00, "EUR", 85.0, "KWH")

    trade = new_trade(buyer_id, seller_id, listing["id"], 100.00)
    completed_trade = wait_for_trade_status(trade["tradeId"], ["COMPLETED"])

    res_listing = requests.get(f"{GATEWAY_URL}/listings/{listing['id']}")
    completed_listing = res_listing.json()

    print(f"Resulting Trade Status: {completed_trade.get('status')}")
    print(f"Resulting Listing Status: {completed_listing.get('status')}")

    assert_equal(completed_trade.get("status"), "COMPLETED", "Trade commit verification")
    assert_equal(completed_listing.get("status"), "COMPLETED", "Listing commit status verification")
    print("[+] Success: Happy path end-to-end transaction pipeline executed flawlessly.")

def run_reservation_failure_scenario(buyer_id, seller_id):
    print("\n=== STARTING SCENARIO: DIRECT RESERVATION FAILURE (LISTING DOES NOT EXIST) ===")
    trade = new_trade(buyer_id, seller_id, 999999999, 100.00)
    failed_trade = wait_for_trade_status(trade["tradeId"], ["FAILED"])

    print(f"Resulting Trade Status: {failed_trade.get('status')}")
    assert_equal(failed_trade.get("status"), "FAILED", "Saga quick structural abortion validation")
    print("[+] Success: Saga intercepted a missing resource and instantly processed failure.")

def main():
    parser = argparse.ArgumentParser(description="Transaction Integration Test Suite")
    parser.add_argument("--reset", action="store_true", help="Purges active container volumes before start")
    parser.add_argument("--skip-build", action="store_true", help="Skips compilation build phase inside Docker Compose")
    args = parser.parse_args()

    if args.reset:
        print("[*] CLEANING EXPIRED PLATFORM VOLUMES AND CONTAINERS...")
        run_command(["docker", "compose", "down", "-v"])

    print("[*] BOOTING SYSTEM INFRASTRUCTURE STACK VIA DOCKER COMPOSE...")
    compose_cmd = ["docker", "compose", "up", "-d"]
    if not args.skip_build:
        compose_cmd.append("--build")
    run_command(compose_cmd)

    print("\n[*] Giving containers 10 seconds to stabilize virtual environments...")
    time.sleep(10)

    print("\n[*] POLLING DOWNSTREAM ACTUATORS UNTIL LIFECYCLE INITIALIZATION COMPLETES...")
    wait_for_service(GATEWAY_URL, "API Gateway Engine")
    wait_for_service(USER_URL, "User Service Core")
    wait_for_service(TRADE_URL, "Trade Service Core")
    wait_for_service(LISTING_URL, "Listing Service Core")
    print("\n[+] ENTIRE ECOSYSTEM STATUS IS HEALTHY! RUNNING SUITES...")

    # --- PREPARE DATA ---
    print("\n=== PREPARING BASE SYSTEM STATE ===")
    # Roles map cleanly to the UserRole enum validation expectations
    b1 = new_user("Buyer One", "buyer1@example.com", "CONSUMER")
    s  = new_user("Solar Seller", "seller@example.com", "PROSUMER")
    b2 = new_user("Broke Buyer", "buyer2@example.com", "CONSUMER")

    # CRITICAL FIX: Extract IDs dynamically from JSON payload responses instead of hardcoding entries
    buyer1_id = b1["id"]
    seller_id = s["id"]
    broke_buyer_id = b2["id"]

    base_listing = new_listing(seller_id, "Saga Wind Array Base", "Baseline high fidelity energy asset.", 50.00, "EUR", 120.0, "KWH")
    base_trade = new_trade(buyer1_id, seller_id, base_listing["id"], 50.00)
    wait_for_trade_status(base_trade["tradeId"], ["COMPLETED"])
    print("[+] System state initialized and baseline trade verified.")

    # Execute Scenarios with the extracted context handles
    run_compensation_scenario(buyer_id=broke_buyer_id, seller_id=seller_id)
    run_success_scenario(buyer_id=buyer1_id, seller_id=seller_id)
    run_reservation_failure_scenario(buyer_id=buyer1_id, seller_id=seller_id)

    print("\n🎉 ALL TRANSACTION INTEGRATION TEST SUITES PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    main()