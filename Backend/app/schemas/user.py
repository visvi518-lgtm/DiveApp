from uuid import UUID

from pydantic import BaseModel, Field

from app.models.enums import AccountStatus, AuthProvider, UserRole


class ProfileSetupRequest(BaseModel):
    nickname: str = Field(min_length=2, max_length=30)
    profile_image_url: str | None = None
    phone_number: str | None = Field(default=None, max_length=20)


class UserProfileResponse(BaseModel):
    nickname: str
    profile_image_url: str | None
    phone_number: str | None
    bio: str | None

    model_config = {"from_attributes": True}


class CurrentUserResponse(BaseModel):
    id: UUID
    email: str
    provider: AuthProvider
    role: UserRole
    account_status: AccountStatus
    profile: UserProfileResponse | None

    model_config = {"from_attributes": True}
