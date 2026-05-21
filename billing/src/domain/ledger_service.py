from datetime import datetime, timezone

from src.domain.models import (
    Authorization,
    Money,
    Receipt,
    Settlement,
    TransactionStatus,
)
from src.ports.repositories import (
    AccountRepository,
    AuthorizationRepository,
    ReceiptRepository,
    SettlementRepository,
)


class InsufficientFundsError(Exception):
    pass


class AuthorizationNotFoundError(Exception):
    pass


class LedgerService:
    def __init__(
        self,
        account_repo: AccountRepository,
        authorization_repo: AuthorizationRepository,
        settlement_repo: SettlementRepository,
        receipt_repo: ReceiptRepository,
    ):
        self._accounts = account_repo
        self._authorizations = authorization_repo
        self._settlements = settlement_repo
        self._receipts = receipt_repo

    async def authorize(
        self,
        trade_id: int,
        buyer_id: int,
        seller_id: int,
        amount: Money,
    ) -> Authorization:
        # Idempotency: if already processed this trade, return existing record
        existing = await self._authorizations.find_by_trade_id(trade_id)
        if existing:
            return existing

        account = await self._accounts.get_by_user_id(buyer_id)

        if not account.can_authorize(amount):
            failed = Authorization(
                id=0,
                trade_id=trade_id,
                buyer_id=buyer_id,
                seller_id=seller_id,
                amount=amount,
                status=TransactionStatus.FAILED,
                created_at=datetime.now(timezone.utc),
            )
            await self._authorizations.save(failed)
            raise InsufficientFundsError(
                f"Insufficient funds: available={account.available.amount} "
                f"{account.available.currency}, required={amount.amount} {amount.currency}"
            )

        # Hold the amount — do not deduct yet, just reserve it
        account.reserved = account.reserved.add(amount)
        await self._accounts.save(account)

        authorization = Authorization(
            id=0,
            trade_id=trade_id,
            buyer_id=buyer_id,
            seller_id=seller_id,
            amount=amount,
            status=TransactionStatus.COMPLETED,
            created_at=datetime.now(timezone.utc),
        )
        return await self._authorizations.save(authorization)

    async def settle(
        self,
        trade_id: int,
        authorization_id: int,
        amount: Money,
    ) -> Settlement:
        # Idempotency
        existing = await self._settlements.find_by_trade_id(trade_id)
        if existing:
            return existing

        # Look up buyer and seller from the stored authorization
        # (SettlePaymentCommand does not include buyerId/sellerId)
        authorization = await self._authorizations.find_by_trade_id(trade_id)
        if not authorization or authorization.status != TransactionStatus.COMPLETED:
            raise AuthorizationNotFoundError(
                f"No completed authorization found for trade {trade_id}"
            )

        buyer_account = await self._accounts.get_by_user_id(authorization.buyer_id)
        seller_account = await self._accounts.get_by_user_id(authorization.seller_id)

        # Release reservation and deduct from buyer
        buyer_account.reserved = buyer_account.reserved.subtract(amount)
        buyer_account.balance = buyer_account.balance.subtract(amount)
        await self._accounts.save(buyer_account)

        # Credit seller
        seller_account.balance = seller_account.balance.add(amount)
        await self._accounts.save(seller_account)

        settlement = Settlement(
            id=0,
            trade_id=trade_id,
            authorization_id=authorization_id,
            amount=amount,
            status=TransactionStatus.COMPLETED,
            created_at=datetime.now(timezone.utc),
        )
        return await self._settlements.save(settlement)

    async def generate_receipt(
        self,
        trade_id: int,
        buyer_id: int,
        seller_id: int,
        listing_id: int,
        amount: Money,
    ) -> Receipt:
        # Idempotency
        existing = await self._receipts.find_by_trade_id(trade_id)
        if existing:
            return existing

        receipt = Receipt(
            id=0,
            trade_id=trade_id,
            buyer_id=buyer_id,
            seller_id=seller_id,
            listing_id=listing_id,
            amount=amount,
            generated_at=datetime.now(timezone.utc),
        )
        return await self._receipts.save(receipt)
