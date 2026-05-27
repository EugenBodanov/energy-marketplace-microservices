from decimal import Decimal
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from src.domain.models import Authorization, Money, TransactionStatus
from src.ports.repositories import AuthorizationRepository
from src.adapters.db.orm import AuthorizationORM


class SqlAuthorizationRepository(AuthorizationRepository):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def find_by_trade_id(self, trade_id: int) -> Authorization | None:
        result = await self._session.execute(
            select(AuthorizationORM).where(AuthorizationORM.trade_id == trade_id)
        )
        row = result.scalar_one_or_none()
        return self._to_domain(row) if row else None

    async def save(self, authorization: Authorization) -> Authorization:
        result = await self._session.execute(
            select(AuthorizationORM).where(
                AuthorizationORM.trade_id == authorization.trade_id
            )
        )
        row = result.scalar_one_or_none()
        if row is None:
            row = AuthorizationORM(
                trade_id=authorization.trade_id,
                buyer_id=authorization.buyer_id,
                seller_id=authorization.seller_id,
                amount_value=authorization.amount.amount,
                amount_currency=authorization.amount.currency,
                status=authorization.status.value,
                created_at=authorization.created_at,
            )
            self._session.add(row)
        else:
            row.buyer_id = authorization.buyer_id
            row.seller_id = authorization.seller_id
            row.amount_value = authorization.amount.amount
            row.amount_currency = authorization.amount.currency
            row.status = authorization.status.value
            row.created_at = authorization.created_at
        await self._session.flush()
        authorization.id = row.id
        return authorization

    def _to_domain(self, row: AuthorizationORM) -> Authorization:
        return Authorization(
            id=row.id,
            trade_id=row.trade_id,
            buyer_id=row.buyer_id,
            seller_id=row.seller_id,
            amount=Money(Decimal(str(row.amount_value)), row.amount_currency),
            status=TransactionStatus(row.status),
            created_at=row.created_at,
        )
