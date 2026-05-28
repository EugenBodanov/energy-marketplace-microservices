import pytest
from decimal import Decimal
from datetime import datetime, timezone

from src.domain.models import (
    Account, Authorization, Settlement, Receipt,
    Money, TransactionStatus,
)
from src.domain.ledger_service import LedgerService, InsufficientFundsError
from src.ports.repositories import (
    AccountRepository, AuthorizationRepository,
    SettlementRepository, ReceiptRepository,
)


# ── In-memory fakes ───────────────────────────────────────────────────────────

class FakeAccountRepo(AccountRepository):
    def __init__(self, accounts: list[Account]):
        self._store = {a.user_id: a for a in accounts}

    async def get_by_user_id(self, user_id: int) -> Account:
        if user_id not in self._store:
            raise ValueError(f"Account not found: {user_id}")
        return self._store[user_id]

    async def save(self, account: Account) -> None:
        self._store[account.user_id] = account


class FakeAuthorizationRepo(AuthorizationRepository):
    def __init__(self):
        self._store: dict[int, Authorization] = {}
        self._next_id = 1

    async def find_by_trade_id(self, trade_id: int) -> Authorization | None:
        return self._store.get(trade_id)

    async def save(self, authorization: Authorization) -> Authorization:
        if authorization.id == 0:
            authorization.id = self._next_id
            self._next_id += 1
        self._store[authorization.trade_id] = authorization
        return authorization


class FakeSettlementRepo(SettlementRepository):
    def __init__(self):
        self._store: dict[int, Settlement] = {}
        self._next_id = 1

    async def find_by_trade_id(self, trade_id: int) -> Settlement | None:
        return self._store.get(trade_id)

    async def save(self, settlement: Settlement) -> Settlement:
        if settlement.id == 0:
            settlement.id = self._next_id
            self._next_id += 1
        self._store[settlement.trade_id] = settlement
        return settlement


class FakeReceiptRepo(ReceiptRepository):
    def __init__(self):
        self._store: dict[int, Receipt] = {}
        self._next_id = 1

    async def find_by_trade_id(self, trade_id: int) -> Receipt | None:
        return self._store.get(trade_id)

    async def save(self, receipt: Receipt) -> Receipt:
        if receipt.id == 0:
            receipt.id = self._next_id
            self._next_id += 1
        self._store[receipt.trade_id] = receipt
        return receipt


# ── Helpers ───────────────────────────────────────────────────────────────────

def eur(amount: str) -> Money:
    return Money(Decimal(amount), "EUR")


def make_account(user_id: int, balance: str, reserved: str = "0.00") -> Account:
    return Account(
        id=user_id,
        user_id=user_id,
        balance=eur(balance),
        reserved=eur(reserved),
        created_at=datetime.now(timezone.utc),
    )


def make_ledger(accounts: list[Account]):
    auth_repo = FakeAuthorizationRepo()
    settlement_repo = FakeSettlementRepo()
    receipt_repo = FakeReceiptRepo()
    ledger = LedgerService(
        FakeAccountRepo(accounts),
        auth_repo,
        settlement_repo,
        receipt_repo,
    )
    return ledger, auth_repo, settlement_repo, receipt_repo


# ── authorize ─────────────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_authorize_reserves_funds():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )

    assert auth.status == TransactionStatus.COMPLETED
    assert auth.id > 0
    assert buyer.reserved.amount == Decimal("300.00")
    assert buyer.balance.amount == Decimal("1000.00")  # total unchanged
    assert buyer.available.amount == Decimal("700.00")  # available reduced


@pytest.mark.asyncio
async def test_authorize_fails_on_insufficient_funds():
    buyer = make_account(user_id=1, balance="100.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    with pytest.raises(InsufficientFundsError) as exc_info:
        await ledger.authorize(
            trade_id=10, buyer_id=1, seller_id=2, amount=eur("500.00")
        )
    assert exc_info.value.authorization_id > 0


@pytest.mark.asyncio
async def test_authorize_is_idempotent():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth1 = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )
    auth2 = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )

    assert auth1.id == auth2.id
    assert buyer.reserved.amount == Decimal("300.00")  # not doubled


# ── settle ────────────────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_settle_transfers_funds():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )
    settlement = await ledger.settle(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )

    assert settlement.status == TransactionStatus.COMPLETED
    assert buyer.balance.amount == Decimal("700.00")
    assert buyer.reserved.amount == Decimal("0.00")
    assert seller.balance.amount == Decimal("300.00")


@pytest.mark.asyncio
async def test_settle_is_idempotent():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )
    s1 = await ledger.settle(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )
    s2 = await ledger.settle(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )

    assert s1.id == s2.id


@pytest.mark.asyncio
async def test_cancel_payment_releases_reserved_funds():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )
    canceled = await ledger.cancel_payment(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )

    assert canceled.status == TransactionStatus.FAILED
    assert buyer.balance.amount == Decimal("1000.00")
    assert buyer.reserved.amount == Decimal("0.00")


@pytest.mark.asyncio
async def test_cancel_payment_is_idempotent():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    auth = await ledger.authorize(
        trade_id=10, buyer_id=1, seller_id=2, amount=eur("300.00")
    )
    first = await ledger.cancel_payment(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )
    second = await ledger.cancel_payment(
        trade_id=10, authorization_id=auth.id, amount=eur("300.00")
    )

    assert first.id == second.id
    assert buyer.reserved.amount == Decimal("0.00")


# ── generate_receipt ──────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_generate_receipt_creates_record():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    receipt = await ledger.generate_receipt(
        trade_id=10,
        buyer_id=1,
        seller_id=2,
        listing_id=99,
        amount=eur("300.00"),
    )

    assert receipt.id > 0
    assert receipt.trade_id == 10
    assert receipt.listing_id == 99
    assert receipt.amount.amount == Decimal("300.00")
    assert receipt.amount.currency == "EUR"


@pytest.mark.asyncio
async def test_generate_receipt_is_idempotent():
    buyer = make_account(user_id=1, balance="1000.00")
    seller = make_account(user_id=2, balance="0.00")
    ledger, _, _, _ = make_ledger([buyer, seller])

    r1 = await ledger.generate_receipt(
        trade_id=10, buyer_id=1, seller_id=2, listing_id=99, amount=eur("300.00")
    )
    r2 = await ledger.generate_receipt(
        trade_id=10, buyer_id=1, seller_id=2, listing_id=99, amount=eur("300.00")
    )

    assert r1.id == r2.id
