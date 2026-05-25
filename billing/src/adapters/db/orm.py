from sqlalchemy import (
    Column, Integer, String, Numeric,
    TIMESTAMP, ForeignKey, Enum as SAEnum,
)
from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    pass


class AccountORM(Base):
    __tablename__ = "accounts"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, unique=True)
    balance_amount = Column(Numeric(18, 2), nullable=False, default=0)
    balance_currency = Column(String(3), nullable=False, default="EUR")
    reserved_amount = Column(Numeric(18, 2), nullable=False, default=0)
    reserved_currency = Column(String(3), nullable=False, default="EUR")
    created_at = Column(TIMESTAMP(timezone=True), nullable=False)


class AuthorizationORM(Base):
    __tablename__ = "authorizations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    trade_id = Column(Integer, nullable=False, unique=True)
    buyer_id = Column(Integer, nullable=False)
    seller_id = Column(Integer, nullable=False)
    amount_value = Column(Numeric(18, 2), nullable=False)
    amount_currency = Column(String(3), nullable=False)
    status = Column(
        SAEnum("COMPLETED", "FAILED", name="authorization_status"),
        nullable=False,
    )
    created_at = Column(TIMESTAMP(timezone=True), nullable=False)


class SettlementORM(Base):
    __tablename__ = "settlements"

    id = Column(Integer, primary_key=True, autoincrement=True)
    trade_id = Column(Integer, nullable=False, unique=True)
    authorization_id = Column(Integer, ForeignKey("authorizations.id"), nullable=False)
    amount_value = Column(Numeric(18, 2), nullable=False)
    amount_currency = Column(String(3), nullable=False)
    status = Column(
        SAEnum("COMPLETED", "FAILED", name="settlement_status"),
        nullable=False,
    )
    created_at = Column(TIMESTAMP(timezone=True), nullable=False)


class ReceiptORM(Base):
    __tablename__ = "receipts"

    id = Column(Integer, primary_key=True, autoincrement=True)
    trade_id = Column(Integer, nullable=False, unique=True)
    buyer_id = Column(Integer, nullable=False)
    seller_id = Column(Integer, nullable=False)
    listing_id = Column(Integer, nullable=False)
    amount_value = Column(Numeric(18, 2), nullable=False)
    amount_currency = Column(String(3), nullable=False)
    generated_at = Column(TIMESTAMP(timezone=True), nullable=False)
