from uuid import UUID

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.schemas.training import TrainingRecordCreateRequest, TrainingRecordResponse, TrainingStatisticsResponse
from app.services.training_service import TrainingService

router = APIRouter(prefix="/api/v1/trainings", tags=["trainings"])


@router.post("", response_model=TrainingRecordResponse, status_code=status.HTTP_201_CREATED)
async def create_training_record(
    body: TrainingRecordCreateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> TrainingRecordResponse:
    service = TrainingService(db)
    record = await service.create(token.user_id, body)
    return TrainingRecordResponse.model_validate(record)


@router.get("", response_model=list[TrainingRecordResponse])
async def list_training_records(
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> list[TrainingRecordResponse]:
    service = TrainingService(db)
    records = await service.list(token.user_id, limit, offset)
    return [TrainingRecordResponse.model_validate(record) for record in records]


@router.get("/statistics", response_model=TrainingStatisticsResponse)
async def get_training_statistics(
    token: TokenPayload = Depends(get_current_token_payload), db: AsyncSession = Depends(get_db)
) -> TrainingStatisticsResponse:
    service = TrainingService(db)
    stats = await service.get_statistics(token.user_id)
    return TrainingStatisticsResponse(**stats)


@router.get("/{record_id}", response_model=TrainingRecordResponse)
async def get_training_record(
    record_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> TrainingRecordResponse:
    service = TrainingService(db)
    record = await service.get(record_id, token.user_id)
    return TrainingRecordResponse.model_validate(record)
