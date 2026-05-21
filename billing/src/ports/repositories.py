from abc import ABC, abstractmethod
from src.domain.models import Account, Authorization, Settlement, Receipt


class AccountRepository(ABC):
    @abstractmethod
    async def get_by_user_id(self, user_id: int) -> Account:
        ...

    @abstractmethod
    async def save(self, account: Account) -> None:
        ...


class AuthorizationRepository(ABC):
    @abstractmethod
    async def find_by_trade_id(self, trade_id: int) -> Authorization | None:
        ...

    @abstractmethod
    async def save(self, authorization: Authorization) -> Authorization:
        """Returns saved authorization with its generated id."""
        ...


class SettlementRepository(ABC):
    @abstractmethod
    async def find_by_trade_id(self, trade_id: int) -> Settlement | None:
        ...

    @abstractmethod
    async def save(self, settlement: Settlement) -> Settlement:
        ...


class ReceiptRepository(ABC):
    @abstractmethod
    async def find_by_trade_id(self, trade_id: int) -> Receipt | None:
        ...

    @abstractmethod
    async def save(self, receipt: Receipt) -> Receipt:
        ...
