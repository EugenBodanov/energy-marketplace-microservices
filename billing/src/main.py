import logging
from contextlib import asynccontextmanager

import aio_pika
from fastapi import FastAPI

from src.adapters.db.orm import Base
from src.adapters.db.session import engine
from src.adapters.http.router import router
from src.adapters.messaging.consumer import start_consumer
from src.config import settings

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Create tables on startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("Database tables ready")

    # Connect to RabbitMQ and start consuming commands
    connection = await aio_pika.connect_robust(settings.rabbitmq_url)
    await start_consumer(connection)
    logger.info("RabbitMQ consumer started")

    yield

    await connection.close()
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
