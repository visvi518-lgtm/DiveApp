from datetime import date
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundException
from app.models.dive_log import DiveLog
from app.models.enums import DiveType
from app.repositories.dive_log_repository import DiveLocationRepository, DiveLogRepository
from app.schemas.dive_log import DiveLogCreateRequest, DiveLogUpdateRequest


class DiveLogService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.location_repository = DiveLocationRepository(db)
        self.dive_log_repository = DiveLogRepository(db)

    async def create(self, user_id: UUID, body: DiveLogCreateRequest) -> DiveLog:
        location = await self.location_repository.get_or_create(
            name=body.location.name,
            address=body.location.address,
            latitude=body.location.latitude,
            longitude=body.location.longitude,
            naver_place_id=body.location.naver_place_id,
            country=body.location.country,
            city=body.location.city,
        )

        dive_log = await self.dive_log_repository.create(
            user_id=user_id,
            location_id=location.id,
            dive_type=body.dive_type,
            dive_date=body.dive_date,
            latitude=body.latitude,
            longitude=body.longitude,
            memo=body.memo,
        )

        if body.dive_type == DiveType.FREEDIVING:
            await self.dive_log_repository.create_freediving_detail(
                dive_log.id, body.freediving.max_depth, body.freediving.dive_time_seconds
            )
        else:
            await self.dive_log_repository.create_scuba_detail(
                dive_log.id,
                body.scuba.max_depth,
                body.scuba.dive_time_seconds,
                body.scuba.tank_pressure_start,
                body.scuba.tank_pressure_end,
            )

        if body.photos:
            await self.dive_log_repository.replace_photos(
                dive_log.id, [(photo.image_url, photo.display_order) for photo in body.photos]
            )

        await self.db.commit()
        return await self._get_or_raise(dive_log.id, user_id)

    async def get(self, dive_log_id: UUID, user_id: UUID) -> DiveLog:
        return await self._get_or_raise(dive_log_id, user_id)

    async def list(
        self,
        user_id: UUID,
        dive_type: DiveType | None,
        date_from: date | None,
        date_to: date | None,
        city: str | None,
        limit: int,
        offset: int,
    ) -> list[DiveLog]:
        return await self.dive_log_repository.list_for_user(
            user_id, dive_type, date_from, date_to, city, limit, offset
        )

    async def update(self, dive_log_id: UUID, user_id: UUID, body: DiveLogUpdateRequest) -> DiveLog:
        dive_log = await self._get_or_raise(dive_log_id, user_id)

        if body.memo is not None:
            dive_log.memo = body.memo

        if body.freediving is not None:
            detail = await self.dive_log_repository.get_freediving_detail(dive_log_id)
            if detail is not None:
                detail.max_depth = body.freediving.max_depth
                detail.dive_time_seconds = body.freediving.dive_time_seconds

        if body.scuba is not None:
            detail = await self.dive_log_repository.get_scuba_detail(dive_log_id)
            if detail is not None:
                detail.max_depth = body.scuba.max_depth
                detail.dive_time_seconds = body.scuba.dive_time_seconds
                detail.tank_pressure_start = body.scuba.tank_pressure_start
                detail.tank_pressure_end = body.scuba.tank_pressure_end

        if body.photos is not None:
            await self.dive_log_repository.replace_photos(
                dive_log_id, [(photo.image_url, photo.display_order) for photo in body.photos]
            )

        await self.db.commit()
        return await self._get_or_raise(dive_log_id, user_id)

    async def delete(self, dive_log_id: UUID, user_id: UUID) -> None:
        dive_log = await self._get_or_raise(dive_log_id, user_id)
        await self.dive_log_repository.soft_delete(dive_log)
        await self.db.commit()

    async def get_statistics(self, user_id: UUID) -> dict:
        return await self.dive_log_repository.get_statistics(user_id)

    async def _get_or_raise(self, dive_log_id: UUID, user_id: UUID) -> DiveLog:
        dive_log = await self.dive_log_repository.get_by_id(dive_log_id, user_id)
        if dive_log is None:
            raise NotFoundException("Dive log not found")
        return dive_log
