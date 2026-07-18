from datetime import date
from uuid import UUID

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.models.enums import DiveType
from app.schemas.dive_log import (
    DiveLogCreateRequest,
    DiveLogListItem,
    DiveLogResponse,
    DiveLogStatisticsResponse,
    DiveLogUpdateRequest,
)
from app.services.dive_log_service import DiveLogService

router = APIRouter(prefix="/api/v1/dive-logs", tags=["dive-logs"])


def _to_list_item(dive_log) -> DiveLogListItem:
    detail = dive_log.freediving or dive_log.scuba
    cover_photo = dive_log.photos[0] if dive_log.photos else None
    return DiveLogListItem(
        id=dive_log.id,
        dive_type=dive_log.dive_type,
        dive_date=dive_log.dive_date,
        location_name=dive_log.location.name,
        cover_image_url=cover_photo.image_url if cover_photo else None,
        max_depth=float(detail.max_depth) if detail else 0.0,
    )


@router.post("", response_model=DiveLogResponse, status_code=status.HTTP_201_CREATED)
async def create_dive_log(
    body: DiveLogCreateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> DiveLogResponse:
    service = DiveLogService(db)
    dive_log = await service.create(token.user_id, body)
    return DiveLogResponse.model_validate(dive_log)


@router.get("", response_model=list[DiveLogListItem])
async def list_dive_logs(
    dive_type: DiveType | None = None,
    date_from: date | None = None,
    date_to: date | None = None,
    city: str | None = None,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> list[DiveLogListItem]:
    service = DiveLogService(db)
    dive_logs = await service.list(token.user_id, dive_type, date_from, date_to, city, limit, offset)
    return [_to_list_item(dive_log) for dive_log in dive_logs]


@router.get("/statistics", response_model=DiveLogStatisticsResponse)
async def get_statistics(
    token: TokenPayload = Depends(get_current_token_payload), db: AsyncSession = Depends(get_db)
) -> DiveLogStatisticsResponse:
    service = DiveLogService(db)
    stats = await service.get_statistics(token.user_id)
    return DiveLogStatisticsResponse(**stats)


@router.get("/{dive_log_id}", response_model=DiveLogResponse)
async def get_dive_log(
    dive_log_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> DiveLogResponse:
    service = DiveLogService(db)
    dive_log = await service.get(dive_log_id, token.user_id)
    return DiveLogResponse.model_validate(dive_log)


@router.patch("/{dive_log_id}", response_model=DiveLogResponse)
async def update_dive_log(
    dive_log_id: UUID,
    body: DiveLogUpdateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> DiveLogResponse:
    service = DiveLogService(db)
    dive_log = await service.update(dive_log_id, token.user_id, body)
    return DiveLogResponse.model_validate(dive_log)


@router.delete("/{dive_log_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_dive_log(
    dive_log_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> None:
    service = DiveLogService(db)
    await service.delete(dive_log_id, token.user_id)
