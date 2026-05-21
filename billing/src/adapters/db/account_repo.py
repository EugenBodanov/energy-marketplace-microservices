from decimal import Decimal
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from src.domain.models import Account, Money
from src.ports.repositories import AccountRepository
from src.adapters.db.orm import AccountORM


class SqlAccountRepository(AccountRepository):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def get_by_user_id(self, user_id: int) -> Account:
        result = await self._session.execute(
            select(AccountORM).where(AccountORM.user_id == user_id)
        )
        row = result.scalar_one_or_none()
        if row is None:
            raise ValueError(f"Account not found for user_id={user_id}")
        return self._to_domain(row)

    async def save(self, account: Account) -> None:
        result = await self._session.execute(
            select(AccountORM).where(AccountORM.id == account.id)
        )
        row = result.scalar_one_or_none()
        if row is None:
            row = AccountORM(
                user_id=account.user_id,
                created_at=account.created_at,
            )
            self._session.add(row)
        row.balance_amount = account.balance.amount
        row.balance_currency = account.balance.currency
        row.reserved_amount = account.reserved.amount
        row.reserved_currency = account.reserved.currency
        await self._session.flush()

    def _to_domain(self, row: AccountORM) -> Account:
        return Account(
            id=row.id,
            user_id=row.user_id,
            balance=Money(Decimal(str(row.balance_amount)), row.balance_currency),
            reserved=Money(Decimal(str(row.reserved_amount)), row.reserved_currency),
            created_at=row.created_at,
        )
