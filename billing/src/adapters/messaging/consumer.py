import logging
from dataclasses import dataclass

import aio_pika
from aio_pika.abc import AbstractIncomingMessage, AbstractQueue, AbstractRobustChannel

from src.adapters.db.session import AsyncSessionFactory
from src.adapters.messaging import handlers
from src.adapters.messaging.publisher import EventPublisher
from src.config import settings

logger = logging.getLogger(__name__)

# Routing keys follow the convention: service.entity.action
# These must match what Trade Service uses when publishing commands
ROUTING_KEY_TO_HANDLER = {
    "billing.authorize.command": handlers.handle_authorize_payment,
    "billing.settle.command": handlers.handle_settle_payment,
    "billing.generate_receipt.command": handlers.handle_generate_receipt,
    "billing.cancel_payment.command": handlers.handle_cancel_payment,
}


@dataclass
class BillingConsumer:
    channel: AbstractRobustChannel
    queue: AbstractQueue
    consumer_tag: str

    async def stop(self) -> None:
        await self.queue.cancel(self.consumer_tag)
        await self.channel.close()


async def start_consumer(connection: aio_pika.abc.AbstractRobustConnection) -> BillingConsumer:
    channel = await connection.channel()
    await channel.set_qos(prefetch_count=10)

    # Declare exchange
    exchange = await channel.declare_exchange(
        settings.exchange_name,
        aio_pika.ExchangeType.DIRECT,
        durable=True,
    )

    # Declare DLQ — receives messages that fail after all retries
    await channel.declare_queue(settings.billing_dlq, durable=True)

    # Declare main queue with DLQ as dead-letter destination
    queue = await channel.declare_queue(
        settings.billing_queue,
        durable=True,
        arguments={
            "x-dead-letter-exchange": "",
            "x-dead-letter-routing-key": settings.billing_dlq,
        },
    )

    for routing_key in ROUTING_KEY_TO_HANDLER:
        await queue.bind(exchange, routing_key=routing_key)

    publisher = EventPublisher(channel)

    async def on_message(message: AbstractIncomingMessage) -> None:
        routing_key = message.routing_key
        handler = ROUTING_KEY_TO_HANDLER.get(routing_key)

        if handler is None:
            logger.warning(f"No handler for routing key: {routing_key} — skipping")
            await message.ack()
            return

        try:
            async with AsyncSessionFactory() as session:
                await handler(message.body, session, publisher)
            await message.ack()
        except Exception as e:
            logger.error(f"Failed to process [{routing_key}]: {e}")
            # nack without requeue → goes to DLQ for manual inspection
            await message.nack(requeue=False)

    consumer_tag = await queue.consume(on_message)
    logger.info(
        f"Consumer started — exchange={settings.exchange_name} "
        f"queue={settings.billing_queue}"
    )
    return BillingConsumer(channel=channel, queue=queue, consumer_tag=consumer_tag)
