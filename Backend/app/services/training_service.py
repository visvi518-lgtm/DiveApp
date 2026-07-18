from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundException
from app.models.training_record import TrainingRecord
from app.repositories.training_repository import TrainingRepository
from app.schemas.training import TrainingRecordCreateRequest


class TrainingService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.training_repository = TrainingRepository(db)

    async def create(self, user_id: UUID, body: TrainingRecordCreateRequest) -> TrainingRecord:
        record = await self.training_repository.create(
            user_id=user_id,
            total_sets=body.total_sets,
            completed_sets=body.completed_sets,
            is_completed=body.is_completed,
            rest_time_seconds=body.rest_time_seconds,
            hold_time_seconds=body.hold_time_seconds,
            rest_interval_seconds=body.rest_interval_seconds,
            hold_interval_seconds=body.hold_interval_seconds,
        )
        await self.db.commit()
        await self.db.refresh(record)
        return record

    async def get(self, record_id: UUID, user_id: UUID) -> TrainingRecord:
        record = await self.training_repository.get_by_id(record_id, user_id)
        if record is None:
            raise NotFoundException("Training record not found")
        return record

    async def list(self, user_id: UUID, limit: int, offset: int) -> list[TrainingRecord]:
        return await self.training_repository.list_for_user(user_id, limit, offset)

    async def get_statistics(self, user_id: UUID) -> dict:
        return await self.training_repository.get_statistics(user_id)
