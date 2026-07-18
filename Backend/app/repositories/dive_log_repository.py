from datetime import date, datetime, timezone
from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.dive_location import DiveLocation
from app.models.dive_log import DiveLog
from app.models.dive_photo import DivePhoto
from app.models.enums import DiveType
from app.models.freediving_log import FreedivingLog
from app.models.scuba_log import ScubaLog


class DiveLocationRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_or_create(
        self,
        name: str,
        address: str | None,
        latitude: float,
        longitude: float,
        naver_place_id: str | None,
        country: str | None,
        city: str | None,
    ) -> DiveLocation:
        stmt = select(DiveLocation).where(
            DiveLocation.name == name,
            DiveLocation.city.is_(city) if city is None else DiveLocation.city == city,
            DiveLocation.country.is_(country) if country is None else DiveLocation.country == country,
            DiveLocation.deleted_at.is_(None),
        )
        result = await self.db.execute(stmt)
        existing = result.scalar_one_or_none()
        if existing is not None:
            return existing

        location = DiveLocation(
            name=name,
            address=address,
            latitude=latitude,
            longitude=longitude,
            naver_place_id=naver_place_id,
            country=country,
            city=city,
        )
        self.db.add(location)
        await self.db.flush()
        return location


class DiveLogRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    def _base_query(self):
        return select(DiveLog).options(
            selectinload(DiveLog.location),
            selectinload(DiveLog.freediving),
            selectinload(DiveLog.scuba),
            selectinload(DiveLog.photos),
        )

    async def get_by_id(self, dive_log_id: UUID, user_id: UUID) -> DiveLog | None:
        stmt = self._base_query().where(
            DiveLog.id == dive_log_id, DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None)
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(
        self,
        user_id: UUID,
        dive_type: DiveType | None,
        date_from: date | None,
        date_to: date | None,
        city: str | None,
        limit: int,
        offset: int,
    ) -> list[DiveLog]:
        stmt = self._base_query().where(DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None))
        if dive_type is not None:
            stmt = stmt.where(DiveLog.dive_type == dive_type)
        if date_from is not None:
            stmt = stmt.where(DiveLog.dive_date >= date_from)
        if date_to is not None:
            stmt = stmt.where(DiveLog.dive_date <= date_to)
        if city is not None:
            stmt = stmt.join(DiveLocation).where(DiveLocation.city == city)
        stmt = stmt.order_by(DiveLog.dive_date.desc()).limit(limit).offset(offset)
        result = await self.db.execute(stmt)
        return list(result.scalars().unique().all())

    async def create(
        self, user_id: UUID, location_id: UUID, dive_type: DiveType, dive_date: date, latitude, longitude, memo
    ) -> DiveLog:
        dive_log = DiveLog(
            user_id=user_id,
            location_id=location_id,
            dive_type=dive_type,
            dive_date=dive_date,
            latitude=latitude,
            longitude=longitude,
            memo=memo,
        )
        self.db.add(dive_log)
        await self.db.flush()
        return dive_log

    async def create_freediving_detail(self, dive_log_id: UUID, max_depth: float, dive_time_seconds: int) -> None:
        self.db.add(
            FreedivingLog(dive_log_id=dive_log_id, max_depth=max_depth, dive_time_seconds=dive_time_seconds)
        )

    async def create_scuba_detail(
        self,
        dive_log_id: UUID,
        max_depth: float,
        dive_time_seconds: int,
        tank_pressure_start: int,
        tank_pressure_end: int,
    ) -> None:
        self.db.add(
            ScubaLog(
                dive_log_id=dive_log_id,
                max_depth=max_depth,
                dive_time_seconds=dive_time_seconds,
                tank_pressure_start=tank_pressure_start,
                tank_pressure_end=tank_pressure_end,
            )
        )

    async def replace_photos(self, dive_log_id: UUID, photos: list[tuple[str, int]]) -> None:
        stmt = select(DivePhoto).where(DivePhoto.dive_log_id == dive_log_id)
        result = await self.db.execute(stmt)
        for photo in result.scalars().all():
            await self.db.delete(photo)
        await self.db.flush()
        for image_url, display_order in photos:
            self.db.add(DivePhoto(dive_log_id=dive_log_id, image_url=image_url, display_order=display_order))

    async def get_freediving_detail(self, dive_log_id: UUID) -> FreedivingLog | None:
        stmt = select(FreedivingLog).where(FreedivingLog.dive_log_id == dive_log_id)
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def get_scuba_detail(self, dive_log_id: UUID) -> ScubaLog | None:
        stmt = select(ScubaLog).where(ScubaLog.dive_log_id == dive_log_id)
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def soft_delete(self, dive_log: DiveLog) -> None:
        dive_log.deleted_at = datetime.now(timezone.utc)

    async def get_statistics(self, user_id: UUID) -> dict:
        base = select(DiveLog).where(DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None))

        total_count_stmt = select(func.count()).select_from(base.subquery())
        total_dive_count = (await self.db.execute(total_count_stmt)).scalar_one()

        freediving_count_stmt = select(func.count()).select_from(
            base.where(DiveLog.dive_type == DiveType.FREEDIVING).subquery()
        )
        freediving_count = (await self.db.execute(freediving_count_stmt)).scalar_one()

        scuba_count_stmt = select(func.count()).select_from(
            base.where(DiveLog.dive_type == DiveType.SCUBA).subquery()
        )
        scuba_count = (await self.db.execute(scuba_count_stmt)).scalar_one()

        max_depth_stmt = select(
            func.coalesce(
                func.greatest(
                    func.max(FreedivingLog.max_depth),
                    func.max(ScubaLog.max_depth),
                ),
                0,
            )
        ).select_from(DiveLog).outerjoin(FreedivingLog, FreedivingLog.dive_log_id == DiveLog.id).outerjoin(
            ScubaLog, ScubaLog.dive_log_id == DiveLog.id
        ).where(
            DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None)
        )
        max_depth_overall = (await self.db.execute(max_depth_stmt)).scalar_one()

        # Sum across both detail tables independently to avoid join fan-out double counting.
        freediving_time_stmt = (
            select(func.coalesce(func.sum(FreedivingLog.dive_time_seconds), 0))
            .select_from(FreedivingLog)
            .join(DiveLog, DiveLog.id == FreedivingLog.dive_log_id)
            .where(DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None))
        )
        scuba_time_stmt = (
            select(func.coalesce(func.sum(ScubaLog.dive_time_seconds), 0))
            .select_from(ScubaLog)
            .join(DiveLog, DiveLog.id == ScubaLog.dive_log_id)
            .where(DiveLog.user_id == user_id, DiveLog.deleted_at.is_(None))
        )
        freediving_time = (await self.db.execute(freediving_time_stmt)).scalar_one()
        scuba_time = (await self.db.execute(scuba_time_stmt)).scalar_one()

        return {
            "total_dive_count": total_dive_count,
            "max_depth_overall": float(max_depth_overall),
            "total_dive_time_seconds": int(freediving_time) + int(scuba_time),
            "freediving_count": freediving_count,
            "scuba_count": scuba_count,
        }
