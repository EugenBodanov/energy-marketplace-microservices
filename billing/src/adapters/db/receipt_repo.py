from decimal import Decimal
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from src.domain.models import Receipt, Money
from src.ports.repositories import ReceiptRepository
from src.adapters.db.orm import ReceiptORM


class SqlReceiptRepository(ReceiptRepository):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def find_by_trade_id(self, trade_id: int) -> Receipt | None:
        result = await self._session.execute(
            select(ReceiptORM).where(ReceiptORM.trade_id == trade_id)
        )
        row = result.scalar_one_or_none()
        return self._to_domain(row) if row else None

    async def save(self, receipt: Receipt) -> Receipt:
        existing = await self.find_by_trade_id(receipt.trade_id)
        if existing:
            return existing
        row = ReceiptORM(
            trade_id=receipt.trade_id,
            buyer_id=receipt.buyer_id,
            seller_id=receipt.seller_id,
            listing_id=receipt.listing_id,
            amount_value=receipt.amount.amount,
            amount_currency=receipt.amount.currency,
            generated_at=receipt.generated_at,
        )
        self._session.add(row)
        await self._session.flush()
        receipt.id = row.id
        return receipt

    def _to_domain(self, row: ReceiptORM) -> Receipt:
        return Receipt(
            id=row.id,
            trade_id=row.trade_id,
            buyer_id=row.buyer_id,
            seller_id=row.seller_id,
            listing_id=row.listing_id,
            amount=Money(Decimal(str(row.amount_value)), row.amount_currency),
            generated_at=row.generated_at,
        )
