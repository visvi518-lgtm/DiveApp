from uuid import UUID

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, require_admin
from app.database.session import get_db
from app.schemas.admin import AdminUserDetailResponse, AdminUserListItem, DashboardResponse
from app.services.admin_service import AdminService

router = APIRouter(prefix="/api/v1/admin", tags=["admin"])


def _user_list_item(user) -> AdminUserListItem:
    return AdminUserListItem(
        id=user.id,
        email=user.email,
        provider=user.provider,
        account_status=user.account_status,
        nickname=user.profile.nickname if user.profile else None,
        created_at=user.created_at,
    )


def _user_detail_response(user) -> AdminUserDetailResponse:
    return AdminUserDetailResponse(
        id=user.id,
        email=user.email,
        provider=user.provider,
        account_status=user.account_status,
        nickname=user.profile.nickname if user.profile else None,
        last_login_at=user.last_login_at,
        created_at=user.created_at,
    )


@router.get("/dashboard", response_model=DashboardResponse)
async def get_dashboard(
    token: TokenPayload = Depends(require_admin), db: AsyncSession = Depends(get_db)
) -> DashboardResponse:
    service = AdminService(db)
    stats = await service.get_dashboard()
    return DashboardResponse(**stats)


@router.get("/users", response_model=list[AdminUserListItem])
async def list_users(
    q: str | None = None,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> list[AdminUserListItem]:
    service = AdminService(db)
    users = await service.list_users(q, limit, offset)
    return [_user_list_item(user) for user in users]


@router.get("/users/{user_id}", response_model=AdminUserDetailResponse)
async def get_user(
    user_id: UUID, token: TokenPayload = Depends(require_admin), db: AsyncSession = Depends(get_db)
) -> AdminUserDetailResponse:
    service = AdminService(db)
    user = await service.get_user(user_id)
    return _user_detail_response(user)


@router.patch("/users/{user_id}/suspend", response_model=AdminUserDetailResponse)
async def suspend_user(
    user_id: UUID, token: TokenPayload = Depends(require_admin), db: AsyncSession = Depends(get_db)
) -> AdminUserDetailResponse:
    service = AdminService(db)
    user = await service.suspend_user(token.user_id, user_id)
    return _user_detail_response(user)


@router.patch("/users/{user_id}/unsuspend", response_model=AdminUserDetailResponse)
async def unsuspend_user(
    user_id: UUID, token: TokenPayload = Depends(require_admin), db: AsyncSession = Depends(get_db)
) -> AdminUserDetailResponse:
    service = AdminService(db)
    user = await service.unsuspend_user(token.user_id, user_id)
    return _user_detail_response(user)
