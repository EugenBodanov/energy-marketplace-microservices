from decimal import Decimal
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from src.domain.models import Settlement, Money, TransactionStatus
from src.ports.repositories import SettlementRepository
from src.adapters.db.orm import SettlementORM


class SqlSettlementRepository(SettlementRepository):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def find_by_trade_id(self, trade_id: int) -> Settlement | None:
        result = await self._session.execute(
            select(SettlementORM).where(SettlementORM.trade_id == trade_id)
        )
        row = result.scalar_one_or_none()
        return self._to_domain(row) if row else None

    async def save(self, settlement: Settlement) -> Settlement:
        row = SettlementORM(
            trade_id=settlement.trade_id,
            authorization_id=settlement.authorization_id,
            amount_value=settlement.amount.amount,
            amount_currency=settlement.amount.currency,
            status=settlement.status.value,
            created_at=settlement.created_at,
        )
        self._session.add(row)
        await self._session.flush()
        settlement.id = row.id
        return settlement

    def _to_domain(self, row: SettlementORM) -> Settlement:
        return Settlement(
            id=row.id,
            trade_id=row.trade_id,
            authorization_id=row.authorization_id,
            amount=Money(Decimal(str(row.amount_value)), row.amount_currency),
            status=TransactionStatus(row.status),
            created_at=row.created_at,
        )
