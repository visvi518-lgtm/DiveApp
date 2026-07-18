from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ConflictException, ForbiddenException, UnauthorizedException
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    hash_password,
    verify_password,
    REFRESH_TOKEN_TYPE,
)
from app.models.enums import AccountStatus, AuthProvider
from app.models.user import User
from app.repositories.user_repository import UserRepository
from app.services.oauth_service import verify_oauth_token


class AuthService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.user_repository = UserRepository(db)

    async def login_with_provider(self, provider: AuthProvider, token: str) -> tuple[User, str, str, bool]:
        user_info = await verify_oauth_token(provider, token)

        user = await self.user_repository.get_by_provider_identity(provider, user_info.provider_user_id)
        is_new_user = user is None

        if user is None:
            user = await self.user_repository.create(
                provider=provider,
                provider_user_id=user_info.provider_user_id,
                email=user_info.email.lower(),
            )
        else:
            self._ensure_account_usable(user)
            if user.account_status == AccountStatus.DORMANT:
                user.account_status = AccountStatus.ACTIVE

        user.last_login_at = datetime.now(timezone.utc)
        await self.db.commit()
        await self.db.refresh(user)

        access_token = create_access_token(user.id, user.role.value)
        refresh_token = create_refresh_token(user.id)
        return user, access_token, refresh_token, is_new_user

    async def register_with_email(self, email: str, password: str) -> tuple[User, str, str, bool]:
        email = email.lower()
        if await self.user_repository.get_by_email(email) is not None:
            raise ConflictException("이미 가입된 이메일입니다.")

        user = await self.user_repository.create(
            provider=AuthProvider.EMAIL,
            provider_user_id=email,
            email=email,
            password_hash=hash_password(password),
        )
        user.last_login_at = datetime.now(timezone.utc)
        await self.db.commit()
        await self.db.refresh(user)

        access_token = create_access_token(user.id, user.role.value)
        refresh_token = create_refresh_token(user.id)
        return user, access_token, refresh_token, True

    async def login_with_email(self, email: str, password: str) -> tuple[User, str, str, bool]:
        email = email.lower()
        user = await self.user_repository.get_by_email(email)
        if user is None or user.password_hash is None or not verify_password(password, user.password_hash):
            raise UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.")

        self._ensure_account_usable(user)
        if user.account_status == AccountStatus.DORMANT:
            user.account_status = AccountStatus.ACTIVE

        user.last_login_at = datetime.now(timezone.utc)
        await self.db.commit()
        await self.db.refresh(user)

        access_token = create_access_token(user.id, user.role.value)
        refresh_token = create_refresh_token(user.id)
        return user, access_token, refresh_token, False

    async def refresh_access_token(self, refresh_token: str) -> str:
        payload = decode_token(refresh_token)
        if payload.get("type") != REFRESH_TOKEN_TYPE:
            raise UnauthorizedException("Refresh token required")

        user = await self.user_repository.get_by_id(UUID(payload["sub"]))
        if user is None:
            raise UnauthorizedException("User not found")
        self._ensure_account_usable(user)

        return create_access_token(user.id, user.role.value)

    @staticmethod
    def _ensure_account_usable(user: User) -> None:
        if user.account_status == AccountStatus.SUSPENDED:
            raise ForbiddenException("Account is suspended")
        if user.account_status == AccountStatus.DELETED:
            raise ForbiddenException("Account has been deleted")
