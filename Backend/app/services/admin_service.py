from datetime import date
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ConflictException, NotFoundException
from app.models.enums import AccountStatus
from app.models.user import User
from app.repositories.admin_log_repository import AdminLogRepository
from app.repositories.admin_repository import AdminDashboardRepository
from app.repositories.user_repository import UserRepository


class AdminService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.dashboard_repository = AdminDashboardRepository(db)
        self.user_repository = UserRepository(db)
        self.admin_log_repository = AdminLogRepository(db)

    async def get_dashboard(self) -> dict:
        return await self.dashboard_repository.get_dashboard_stats(date.today())

    async def list_users(self, query: str | None, limit: int, offset: int) -> list[User]:
        return await self.user_repository.list_for_admin(query, limit, offset)

    async def get_user(self, user_id: UUID) -> User:
        return await self._get_user_or_raise(user_id)

    async def suspend_user(self, admin_user_id: UUID, user_id: UUID) -> User:
        user = await self._get_user_or_raise(user_id)
        if user.account_status == AccountStatus.SUSPENDED:
            raise ConflictException("User is already suspended")
        user.account_status = AccountStatus.SUSPENDED
        await self.admin_log_repository.record(admin_user_id, "SUSPEND_USER", "User", user_id)
        await self.db.commit()
        await self.db.refresh(user)
        return user

    async def unsuspend_user(self, admin_user_id: UUID, user_id: UUID) -> User:
        user = await self._get_user_or_raise(user_id)
        if user.account_status != AccountStatus.SUSPENDED:
            raise ConflictException("User is not suspended")
        user.account_status = AccountStatus.ACTIVE
        await self.admin_log_repository.record(admin_user_id, "UNSUSPEND_USER", "User", user_id)
        await self.db.commit()
        await self.db.refresh(user)
        return user

    async def _get_user_or_raise(self, user_id: UUID) -> User:
        user = await self.user_repository.get_by_id(user_id)
        if user is None:
            raise NotFoundException("User not found")
        return user
