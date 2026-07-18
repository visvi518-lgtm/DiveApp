from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ConflictException, NotFoundException
from app.models.banner import Banner
from app.repositories.admin_log_repository import AdminLogRepository
from app.repositories.banner_repository import BannerRepository
from app.schemas.banner import BannerCreateRequest, BannerUpdateRequest


class BannerService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.banner_repository = BannerRepository(db)
        self.admin_log_repository = AdminLogRepository(db)

    async def create(self, admin_user_id: UUID, body: BannerCreateRequest) -> Banner:
        await self._ensure_display_order_available(body.display_order)
        banner = await self.banner_repository.create(**body.model_dump())
        await self.admin_log_repository.record(admin_user_id, "CREATE_BANNER", "Banner", banner.id)
        await self.db.commit()
        await self.db.refresh(banner)
        return banner

    async def list_active(self) -> list[Banner]:
        return await self.banner_repository.list_active(datetime.now(timezone.utc))

    async def list_all(self, limit: int, offset: int) -> list[Banner]:
        return await self.banner_repository.list_all(limit, offset)

    async def update(self, admin_user_id: UUID, banner_id: UUID, body: BannerUpdateRequest) -> Banner:
        banner = await self._get_or_raise(banner_id)
        updates = body.model_dump(exclude_unset=True)
        if "display_order" in updates:
            await self._ensure_display_order_available(updates["display_order"], exclude_id=banner_id)
        for field, value in updates.items():
            setattr(banner, field, value)
        await self.admin_log_repository.record(admin_user_id, "UPDATE_BANNER", "Banner", banner_id)
        await self.db.commit()
        await self.db.refresh(banner)
        return banner

    async def delete(self, admin_user_id: UUID, banner_id: UUID) -> None:
        banner = await self._get_or_raise(banner_id)
        banner.deleted_at = datetime.now(timezone.utc)
        await self.admin_log_repository.record(admin_user_id, "DELETE_BANNER", "Banner", banner_id)
        await self.db.commit()

    async def _ensure_display_order_available(self, display_order: int, exclude_id: UUID | None = None) -> None:
        existing = await self.banner_repository.get_by_display_order(display_order, exclude_id)
        if existing is not None:
            raise ConflictException(f"display_order {display_order} is already in use")

    async def _get_or_raise(self, banner_id: UUID) -> Banner:
        banner = await self.banner_repository.get_by_id(banner_id)
        if banner is None:
            raise NotFoundException("Banner not found")
        return banner
