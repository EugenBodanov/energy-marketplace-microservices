import logging
import asyncio
from contextlib import asynccontextmanager, suppress

import aio_pika
from fastapi import FastAPI

from src.adapters.db.orm import Base
from src.adapters.db.session import engine
from src.adapters.http.router import router
from src.adapters.messaging.consumer import start_consumer
from src.config import settings

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


async def run_rabbitmq_consumer() -> None:
    while True:
        connection = None
        try:
            connection = await aio_pika.connect_robust(
                settings.rabbitmq_url,
                timeout=10,
            )
            await start_consumer(connection)
            logger.info("RabbitMQ consumer connected")
            await asyncio.Future()
        except asyncio.CancelledError:
            if connection and not connection.is_closed:
                await connection.close()
            raise
        except Exception:
            logger.exception("RabbitMQ consumer is not ready; retrying in 5 seconds")
            if connection and not connection.is_closed:
                await connection.close()
            await asyncio.sleep(5)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Create tables on startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("Database tables ready")

    consumer_task = asyncio.create_task(run_rabbitmq_consumer())

    yield

    consumer_task.cancel()
    with suppress(asyncio.CancelledError):
        await consumer_task
    await engine.dispose()
    logger.info("Billing service shut down")


app = FastAPI(
    title="Billing Service",
    description="Internal billing service for the energy marketplace Saga.",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(router)


@app.get("/health")
async def health():
    return {"status": "ok"}
