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
    tradeId: int
    receiptId: int
    buyerId: int
    sellerId: int
    listingId: int
    currency: str
    amount: Decimal
    generatedAt: datetime


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


@router.get("/receipts/{receipt_id}", response_model=ReceiptResponse)
async def get_receipt(receipt_id: int, session: AsyncSession = Depends(get_session)):
    repo = SqlReceiptRepository(session)
    receipt = await repo.find_by_id(receipt_id)
    if receipt is None:
        raise HTTPException(status_code=404, detail=f"Receipt not found for id {receipt_id}")
    return ReceiptResponse(
        tradeId=receipt.trade_id,
        receiptId=receipt.id,
        buyerId=receipt.buyer_id,
        sellerId=receipt.seller_id,
        listingId=receipt.listing_id,
        currency=receipt.amount.currency,
        amount=receipt.amount.amount,
        generatedAt=receipt.generated_at,
    )
