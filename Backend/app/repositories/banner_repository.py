from datetime import datetime
from uuid import UUID

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.banner import Banner


class BannerRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, **fields) -> Banner:
        banner = Banner(**fields)
        self.db.add(banner)
        await self.db.flush()
        return banner

    async def get_by_id(self, banner_id: UUID) -> Banner | None:
        stmt = select(Banner).where(Banner.id == banner_id, Banner.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_active(self, now: datetime) -> list[Banner]:
        stmt = (
            select(Banner)
            .where(
                Banner.deleted_at.is_(None),
                Banner.is_active.is_(True),
                or_(Banner.start_at.is_(None), Banner.start_at <= now),
                or_(Banner.end_at.is_(None), Banner.end_at >= now),
            )
            .order_by(Banner.display_order.asc(), Banner.created_at.asc())
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def get_by_display_order(self, display_order: int, exclude_id: UUID | None = None) -> Banner | None:
        stmt = select(Banner).where(Banner.display_order == display_order, Banner.deleted_at.is_(None))
        if exclude_id is not None:
            stmt = stmt.where(Banner.id != exclude_id)
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_all(self, limit: int, offset: int) -> list[Banner]:
        stmt = (
            select(Banner)
            .where(Banner.deleted_at.is_(None))
            .order_by(Banner.display_order.asc(), Banner.created_at.asc())
            .limit(limit)
            .offset(offset)
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().all())
