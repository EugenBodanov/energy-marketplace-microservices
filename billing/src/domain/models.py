from dataclasses import dataclass
from decimal import Decimal
from enum import Enum
from datetime import datetime


class TransactionStatus(str, Enum):
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


@dataclass
class Money:
    """
    Mirrors the Java Money value object exactly.
    JSON shape: {"amount": "100.00", "currency": "EUR"}
    """
    amount: Decimal
    currency: str

    def can_cover(self, other: "Money") -> bool:
        if self.currency != other.currency:
            raise ValueError(f"Currency mismatch: {self.currency} != {other.currency}")
        return self.amount >= other.amount

    def subtract(self, other: "Money") -> "Money":
        if self.currency != other.currency:
            raise ValueError(f"Currency mismatch: {self.currency} != {other.currency}")
        result = self.amount - other.amount
        if result < 0:
            raise ValueError("Result would be negative")
        return Money(amount=result, currency=self.currency)

    def add(self, other: "Money") -> "Money":
        if self.currency != other.currency:
            raise ValueError(f"Currency mismatch: {self.currency} != {other.currency}")
        return Money(amount=self.amount + other.amount, currency=self.currency)


@dataclass
class Account:
    id: int
    user_id: int
    balance: Money
    reserved: Money
    created_at: datetime

    @property
    def available(self) -> Money:
        return self.balance.subtract(self.reserved)

    def can_authorize(self, amount: Money) -> bool:
        return self.available.can_cover(amount)


@dataclass
class Authorization:
    """
    Stored when AuthorizePaymentCommand is processed.
    Holds buyer_id and seller_id so SettlePaymentCommand
    (which does not include them) can look them up later.
    """
    id: int
    trade_id: int
    buyer_id: int
    seller_id: int
    amount: Money
    status: TransactionStatus
    created_at: datetime


@dataclass
class Settlement:
    id: int
    trade_id: int
    authorization_id: int
    amount: Money
    status: TransactionStatus
    created_at: datetime


@dataclass
class Receipt:
    id: int
    trade_id: int
    buyer_id: int
    seller_id: int
    listing_id: int
    amount: Money
    generated_at: datetime
