from datetime import datetime
from decimal import Decimal

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from src.adapters.db.account_repo import SqlAccountRepository
from src.adapters.db.receipt_repo import SqlReceiptRepository
from src.adapters.db.session import get_session

router = APIRouter(prefix="/billing")


class MoneyResponse(BaseModel):
    amount: Decimal
    currency: str


class AccountResponse(BaseModel):
    user_id: int
    balance: MoneyResponse
    reserved: MoneyResponse
    available: MoneyResponse


class ReceiptResponse(BaseModel):
    id: int
    trade_id: int
    buyer_id: int
    seller_id: int
    listing_id: int
    amount: MoneyResponse
    generated_at: datetime


@router.get("/accounts/{user_id}", response_model=AccountResponse)
async def get_account(user_id: int, session: AsyncSession = Depends(get_session)):
    repo = SqlAccountRepository(session)
    try:
        account = await repo.get_by_user_id(user_id)
    except ValueError:
        raise HTTPException(status_code=404, detail=f"Account not found for user {user_id}")
    return AccountResponse(
        user_id=account.user_id,
        balance=MoneyResponse(amount=account.balance.amount, currency=account.balance.currency),
        reserved=MoneyResponse(amount=account.reserved.amount, currency=account.reserved.currency),
        available=MoneyResponse(amount=account.available.amount, currency=account.available.currency),
    )


@router.get("/receipts/{trade_id}", response_model=ReceiptResponse)
async def get_receipt(trade_id: int, session: AsyncSession = Depends(get_session)):
    repo = SqlReceiptRepository(session)
    receipt = await repo.find_by_trade_id(trade_id)
    if receipt is None:
        raise HTTPException(status_code=404, detail=f"Receipt not found for trade {trade_id}")
    return ReceiptResponse(
        id=receipt.id,
        trade_id=receipt.trade_id,
        buyer_id=receipt.buyer_id,
        seller_id=receipt.seller_id,
        listing_id=receipt.listing_id,
        amount=MoneyResponse(amount=receipt.amount.amount, currency=receipt.amount.currency),
        generated_at=receipt.generated_at,
    )
