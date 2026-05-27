import json
import logging
from datetime import datetime, timezone
from decimal import Decimal

import aio_pika

from src.config import settings
from src.domain.models import Money

logger = logging.getLogger(__name__)


def _serialize(obj):
    if isinstance(obj, Decimal):
        return str(obj)
    if isinstance(obj, datetime):
        return obj.isoformat()
    raise TypeError(f"Not serializable: {type(obj)}")


class EventPublisher:
    def __init__(self, channel: aio_pika.abc.AbstractChannel):
        self._channel = channel

    async def _publish(self, routing_key: str, payload: dict) -> None:
        exchange = await self._channel.declare_exchange(
            settings.exchange_name,
            aio_pika.ExchangeType.DIRECT,
            durable=True,
        )
        body = json.dumps(payload, default=_serialize).encode()
        await exchange.publish(
            aio_pika.Message(
                body=body,
                content_type="application/json",
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
            ),
            routing_key=routing_key,
        )
        logger.info(f"Published [{routing_key}] trade_id={payload.get('tradeId')}")

    async def payment_authorized(
        self,
        trade_id: int,
        payment_authorization_id: int,
        authorized_amount: Money,
    ) -> None:
        """
        Matches HandlePaymentAuthorizedCommand (command/in/).
        Fields: tradeId, paymentAuthorizationId, authorizedAmount, occurredAt
        """
        await self._publish(
            "payment.authorized.event",
            {
                "eventType": "PAYMENT_AUTHORIZED",
                "tradeId": trade_id,
                "paymentAuthorizationId": payment_authorization_id,
                "authorizedAmount": authorized_amount.amount,
                "currency": authorized_amount.currency,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def payment_authorization_failed(
        self,
        trade_id: int,
        payment_authorization_id: int,
    ) -> None:
        await self._publish(
            "payment.authorization_failed.event",
            {
                "eventType": "PAYMENT_AUTHORIZATION_FAILED",
                "tradeId": trade_id,
                "paymentAuthorizationId": payment_authorization_id,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def payment_settled(
        self,
        trade_id: int,
        payment_settlement_id: int,
        settled_amount: Money,
    ) -> None:
        """
        Matches HandlePaymentSettledCommand (command/in/).
        Fields: tradeId, paymentSettlementId, settledAmount, occurredAt
        """
        await self._publish(
            "payment.settled.event",
            {
                "eventType": "PAYMENT_SETTLED",
                "tradeId": trade_id,
                "paymentSettlementId": payment_settlement_id,
                "settledAmount": settled_amount.amount,
                "currency": settled_amount.currency,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def payment_settlement_failed(
        self,
        trade_id: int,
        payment_authorization_id: int,
    ) -> None:
        await self._publish(
            "payment.settlement_failed.event",
            {
                "eventType": "PAYMENT_SETTLEMENT_FAILED",
                "tradeId": trade_id,
                "paymentAuthorizationId": payment_authorization_id,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def receipt_generated(
        self,
        trade_id: int,
        receipt_id: int,
    ) -> None:
        """
        Matches HandleReceiptGeneratedCommand (command/in/).
        Fields: tradeId, receiptId, occurredAt
        """
        await self._publish(
            "receipt.generated.event",
            {
                "eventType": "RECEIPT_GENERATED",
                "tradeId": trade_id,
                "receiptId": receipt_id,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def cancel_payment_success(
        self,
        trade_id: int,
        payment_authorization_id: int,
    ) -> None:
        await self._publish(
            "payment.rolled_back.event",
            {
                "eventType": "CANCEL_PAYMENT_SUCCESS",
                "tradeId": trade_id,
                "paymentAuthorizationId": payment_authorization_id,
                "occurredAt": datetime.now(timezone.utc),
            },
        )

    async def cancel_payment_failed(
        self,
        trade_id: int,
        payment_authorization_id: int,
    ) -> None:
        await self._publish(
            "payment.rolled_back.event",
            {
                "eventType": "CANCEL_PAYMENT_FAILED",
                "tradeId": trade_id,
                "paymentAuthorizationId": payment_authorization_id,
                "occurredAt": datetime.now(timezone.utc),
            },
        )
