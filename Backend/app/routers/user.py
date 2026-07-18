from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.schemas.user import CurrentUserResponse, ProfileSetupRequest, UserProfileResponse
from app.services.user_service import UserService

router = APIRouter(prefix="/api/v1/users", tags=["users"])


@router.get("/me", response_model=CurrentUserResponse)
async def get_me(
    token: TokenPayload = Depends(get_current_token_payload), db: AsyncSession = Depends(get_db)
) -> CurrentUserResponse:
    service = UserService(db)
    user = await service.get_current_user(token.user_id)
    return CurrentUserResponse.model_validate(user)


@router.post("/me/profile", response_model=UserProfileResponse, status_code=status.HTTP_201_CREATED)
async def setup_profile(
    body: ProfileSetupRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> UserProfileResponse:
    service = UserService(db)
    profile = await service.setup_profile(
        user_id=token.user_id,
        nickname=body.nickname,
        profile_image_url=body.profile_image_url,
        phone_number=body.phone_number,
    )
    return UserProfileResponse.model_validate(profile)
