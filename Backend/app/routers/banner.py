from uuid import UUID

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, require_admin
from app.database.session import get_db
from app.schemas.banner import BannerCreateRequest, BannerResponse, BannerUpdateRequest
from app.services.banner_service import BannerService

router = APIRouter(prefix="/api/v1/banners", tags=["banners"])
admin_router = APIRouter(prefix="/api/v1/admin/banners", tags=["admin-banners"])


@router.get("", response_model=list[BannerResponse])
async def list_active_banners(db: AsyncSession = Depends(get_db)) -> list[BannerResponse]:
    service = BannerService(db)
    banners = await service.list_active()
    return [BannerResponse.model_validate(banner) for banner in banners]


@admin_router.post("", response_model=BannerResponse, status_code=status.HTTP_201_CREATED)
async def create_banner(
    body: BannerCreateRequest,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> BannerResponse:
    service = BannerService(db)
    banner = await service.create(token.user_id, body)
    return BannerResponse.model_validate(banner)


@admin_router.get("", response_model=list[BannerResponse])
async def list_all_banners(
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> list[BannerResponse]:
    service = BannerService(db)
    banners = await service.list_all(limit, offset)
    return [BannerResponse.model_validate(banner) for banner in banners]


@admin_router.patch("/{banner_id}", response_model=BannerResponse)
async def update_banner(
    banner_id: UUID,
    body: BannerUpdateRequest,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> BannerResponse:
    service = BannerService(db)
    banner = await service.update(token.user_id, banner_id, body)
    return BannerResponse.model_validate(banner)


@admin_router.delete("/{banner_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_banner(
    banner_id: UUID,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> None:
    service = BannerService(db)
    await service.delete(token.user_id, banner_id)
