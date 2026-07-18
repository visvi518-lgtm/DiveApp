from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.models.enums import AuthProvider
from app.schemas.auth import (
    AccessTokenResponse,
    EmailLoginRequest,
    EmailRegisterRequest,
    RefreshTokenRequest,
    SocialLoginRequest,
    TokenResponse,
)
from app.services.auth_service import AuthService

router = APIRouter(prefix="/api/v1/auth", tags=["auth"])


# NOTE: these two static routes must stay registered before /login/{provider}
# below — FastAPI/Starlette matches routes in declaration order, and
# /login/{provider} would otherwise structurally match "/login/email" first
# and fail enum validation instead of falling through to this handler.
@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(body: EmailRegisterRequest, db: AsyncSession = Depends(get_db)) -> TokenResponse:
    service = AuthService(db)
    _, access_token, refresh_token, is_new_user = await service.register_with_email(body.email, body.password)
    return TokenResponse(access_token=access_token, refresh_token=refresh_token, is_new_user=is_new_user)


@router.post("/login/email", response_model=TokenResponse)
async def login_email(body: EmailLoginRequest, db: AsyncSession = Depends(get_db)) -> TokenResponse:
    service = AuthService(db)
    _, access_token, refresh_token, is_new_user = await service.login_with_email(body.email, body.password)
    return TokenResponse(access_token=access_token, refresh_token=refresh_token, is_new_user=is_new_user)


@router.post("/login/{provider}", response_model=TokenResponse)
async def login(provider: AuthProvider, body: SocialLoginRequest, db: AsyncSession = Depends(get_db)) -> TokenResponse:
    service = AuthService(db)
    _, access_token, refresh_token, is_new_user = await service.login_with_provider(provider, body.token)
    return TokenResponse(access_token=access_token, refresh_token=refresh_token, is_new_user=is_new_user)


@router.post("/refresh", response_model=AccessTokenResponse)
async def refresh(body: RefreshTokenRequest, db: AsyncSession = Depends(get_db)) -> AccessTokenResponse:
    service = AuthService(db)
    access_token = await service.refresh_access_token(body.refresh_token)
    return AccessTokenResponse(access_token=access_token)


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(token: TokenPayload = Depends(get_current_token_payload)) -> None:
    """Stateless MVP logout: the client discards its tokens. Reserved for future
    server-side session/blacklist tracking without breaking the client contract."""
    return None
