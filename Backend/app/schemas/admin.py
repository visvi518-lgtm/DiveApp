from datetime import datetime
from uuid import UUID

from pydantic import BaseModel

from app.models.enums import AccountStatus, AuthProvider


class DashboardResponse(BaseModel):
    total_user_count: int
    new_user_count_today: int
    active_user_count: int
    post_count: int
    comment_count: int
    dive_log_count: int


class AdminUserListItem(BaseModel):
    id: UUID
    email: str
    provider: AuthProvider
    account_status: AccountStatus
    nickname: str | None
    created_at: datetime


class AdminUserDetailResponse(BaseModel):
    id: UUID
    email: str
    provider: AuthProvider
    account_status: AccountStatus
    nickname: str | None
    last_login_at: datetime | None
    created_at: datetime
