from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.models.admin_log import AdminLog


class AdminLogRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def record(
        self,
        admin_user_id: UUID,
        action: str,
        target_type: str,
        target_id: UUID | None,
        description: str | None = None,
    ) -> AdminLog:
        log = AdminLog(
            admin_user_id=admin_user_id,
            action=action,
            target_type=target_type,
            target_id=target_id,
            description=description,
        )
        self.db.add(log)
        await self.db.flush()
        return log
