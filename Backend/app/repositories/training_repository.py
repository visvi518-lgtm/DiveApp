from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.training_record import TrainingRecord


class TrainingRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(
        self,
        user_id: UUID,
        total_sets: int,
        completed_sets: int,
        is_completed: bool,
        rest_time_seconds: int,
        hold_time_seconds: int,
        rest_interval_seconds: int,
        hold_interval_seconds: int,
    ) -> TrainingRecord:
        record = TrainingRecord(
            user_id=user_id,
            total_sets=total_sets,
            completed_sets=completed_sets,
            is_completed=is_completed,
            rest_time_seconds=rest_time_seconds,
            hold_time_seconds=hold_time_seconds,
            rest_interval_seconds=rest_interval_seconds,
            hold_interval_seconds=hold_interval_seconds,
        )
        self.db.add(record)
        await self.db.flush()
        return record

    async def get_by_id(self, record_id: UUID, user_id: UUID) -> TrainingRecord | None:
        stmt = select(TrainingRecord).where(
            TrainingRecord.id == record_id,
            TrainingRecord.user_id == user_id,
            TrainingRecord.deleted_at.is_(None),
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(self, user_id: UUID, limit: int, offset: int) -> list[TrainingRecord]:
        stmt = (
            select(TrainingRecord)
            .where(TrainingRecord.user_id == user_id, TrainingRecord.deleted_at.is_(None))
            .order_by(TrainingRecord.completed_at.desc())
            .limit(limit)
            .offset(offset)
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def get_statistics(self, user_id: UUID) -> dict:
        base = select(TrainingRecord).where(TrainingRecord.user_id == user_id, TrainingRecord.deleted_at.is_(None))

        total_count = (await self.db.execute(select(func.count()).select_from(base.subquery()))).scalar_one()

        completed_count = (
            await self.db.execute(
                select(func.count()).select_from(
                    base.where(TrainingRecord.is_completed.is_(True)).subquery()
                )
            )
        ).scalar_one()

        average_completed_sets = (
            await self.db.execute(
                select(func.coalesce(func.avg(TrainingRecord.completed_sets), 0)).select_from(base.subquery())
            )
        ).scalar_one()

        last_training_at = (
            await self.db.execute(
                select(func.max(TrainingRecord.completed_at)).select_from(base.subquery())
            )
        ).scalar_one()

        completion_rate = (completed_count / total_count) if total_count > 0 else 0.0

        return {
            "total_training_count": total_count,
            "completion_rate": completion_rate,
            "average_completed_sets": float(average_completed_sets),
            "last_training_at": last_training_at,
        }
