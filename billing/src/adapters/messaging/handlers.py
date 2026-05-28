import json
import logging
from decimal import Decimal

from sqlalchemy.ext.asyncio import AsyncSession

from src.adapters.db.account_repo import SqlAccountRepository
from src.adapters.db.authorization_repo import SqlAuthorizationRepository
from src.adapters.db.receipt_repo import SqlReceiptRepository
from src.adapters.db.settlement_repo import SqlSettlementRepository
from src.adapters.messaging.publisher import EventPublisher
from src.domain.ledger_service import (
    AuthorizationNotFoundError,
    InsufficientFundsError,
    LedgerService,
)
from src.domain.models import Money

logger = logging.getLogger(__name__)


def _currency_code(value) -> str:
    if isinstance(value, dict):
        return value.get("currencyCode") or value.get("currency") or value.get("code")
    return str(value)


def _parse_money(data: dict) -> Money:
    """Parses Trade money payloads in flat or nested JSON shape."""
    amount = data["amount"]
    currency = data.get("currency")
    if isinstance(amount, dict):
        currency = amount["currency"]
        amount = amount["amount"]
    return Money(
        amount=Decimal(str(amount)),
        currency=_currency_code(currency),
    )


def _build_ledger(session: AsyncSession) -> LedgerService:
    return LedgerService(
        account_repo=SqlAccountRepository(session),
        authorization_repo=SqlAuthorizationRepository(session),
        settlement_repo=SqlSettlementRepository(session),
        receipt_repo=SqlReceiptRepository(session),
    )


async def handle_authorize_payment(
    body: bytes,
    session: AsyncSession,
    publisher: EventPublisher,
) -> None:
    """
    Receives AuthorizePaymentCommand (command/out/).
    Fields: tradeId, buyerId, sellerId, amount, requestedAt
    """
    data = json.loads(body)
    trade_id = int(data["tradeId"])
    buyer_id = int(data["buyerId"])
    seller_id = int(data["sellerId"])
    amount = _parse_money(data)

    logger.info(
        f"[AuthorizePayment] trade={trade_id} buyer={buyer_id} "
        f"seller={seller_id} amount={amount.amount} {amount.currency}"
    )

    ledger = _build_ledger(session)
    try:
        auth_error = None
        auth = None
        async with session.begin():
            try:
                auth = await ledger.authorize(trade_id, buyer_id, seller_id, amount)
            except InsufficientFundsError as e:
                auth_error = e
        if auth_error:
            logger.warning(f"[AuthorizePayment] Insufficient funds: {auth_error}")
            await publisher.payment_authorization_failed(
                trade_id=trade_id,
                payment_authorization_id=auth_error.authorization_id,
            )
            return
        await publisher.payment_authorized(
            trade_id=trade_id,
            payment_authorization_id=auth.id,
            authorized_amount=auth.amount,
        )
    except Exception as e:
        logger.error(f"[AuthorizePayment] Unexpected error: {e}", exc_info=True)
        await publisher.payment_authorization_failed(
            trade_id=trade_id,
            payment_authorization_id=0,
        )
        raise


async def handle_settle_payment(
    body: bytes,
    session: AsyncSession,
    publisher: EventPublisher,
) -> None:
    """
    Receives SettlePaymentCommand (command/out/).
    Fields: tradeId, paymentAuthorizationId, amount, requestedAt
    Note: no buyerId/sellerId — looked up from stored authorization.
    """
    data = json.loads(body)
    trade_id = int(data["tradeId"])
    authorization_id = int(data["paymentAuthorizationId"])
    amount = _parse_money(data)

    logger.info(
        f"[SettlePayment] trade={trade_id} auth={authorization_id} "
        f"amount={amount.amount} {amount.currency}"
    )

    ledger = _build_ledger(session)
    try:
        async with session.begin():
            settlement = await ledger.settle(trade_id, authorization_id, amount)
        await publisher.payment_settled(
            trade_id=trade_id,
            payment_settlement_id=settlement.id,
            settled_amount=settlement.amount,
        )
    except AuthorizationNotFoundError as e:
        logger.error(f"[SettlePayment] Authorization not found: {e}")
        await publisher.payment_settlement_failed(
            trade_id=trade_id,
            payment_authorization_id=authorization_id,
        )
    except Exception as e:
        logger.error(f"[SettlePayment] Error: {e}", exc_info=True)
        await publisher.payment_settlement_failed(
            trade_id=trade_id,
            payment_authorization_id=authorization_id,
        )
        raise


async def handle_generate_receipt(
    body: bytes,
    session: AsyncSession,
    publisher: EventPublisher,
) -> None:
    """
    Receives GenerateReceiptCommand (command/out/).
    Fields: tradeId, buyerId, sellerId, listingId, amount, tradeCompletedAt
    This is a separate command — not auto-triggered after settle.
    """
    data = json.loads(body)
    trade_id = int(data["tradeId"])
    buyer_id = int(data["buyerId"])
    seller_id = int(data["sellerId"])
    listing_id = int(data["listingId"])
    amount = _parse_money(data)

    logger.info(
        f"[GenerateReceipt] trade={trade_id} buyer={buyer_id} "
        f"seller={seller_id} listing={listing_id}"
    )

    ledger = _build_ledger(session)
    try:
        async with session.begin():
            receipt = await ledger.generate_receipt(
                trade_id, buyer_id, seller_id, listing_id, amount
            )
        await publisher.receipt_generated(
            trade_id=trade_id,
            receipt_id=receipt.id,
        )
    except Exception as e:
        logger.error(f"[GenerateReceipt] Error: {e}", exc_info=True)
        raise


async def handle_cancel_payment(
    body: bytes,
    session: AsyncSession,
    publisher: EventPublisher,
) -> None:
    """
    Receives CancelPaymentCommand.
    Fields: tradeId, paymentAuthorizationId, amount, requestedAt
    """
    data = json.loads(body)
    trade_id = int(data["tradeId"])
    authorization_id = int(data["paymentAuthorizationId"])
    amount = _parse_money(data)

    logger.info(
        f"[CancelPayment] trade={trade_id} auth={authorization_id} "
        f"amount={amount.amount} {amount.currency}"
    )

    ledger = _build_ledger(session)
    try:
        async with session.begin():
            auth = await ledger.cancel_payment(trade_id, authorization_id, amount)
        await publisher.cancel_payment_success(
            trade_id=trade_id,
            payment_authorization_id=auth.id,
        )
    except AuthorizationNotFoundError as e:
        logger.error(f"[CancelPayment] Authorization not found: {e}")
        await publisher.cancel_payment_failed(
            trade_id=trade_id,
            payment_authorization_id=authorization_id,
        )
    except Exception as e:
        logger.error(f"[CancelPayment] Error: {e}", exc_info=True)
        await publisher.cancel_payment_failed(
            trade_id=trade_id,
            payment_authorization_id=authorization_id,
        )
        raise
