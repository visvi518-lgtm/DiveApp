from uuid import UUID

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.enums import AuthProvider
from app.models.user import User
from app.models.user_profile import UserProfile


class UserRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_id(self, user_id: UUID) -> User | None:
        stmt = select(User).options(selectinload(User.profile)).where(User.id == user_id, User.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_provider_identity(self, provider: AuthProvider, provider_user_id: str) -> User | None:
        stmt = select(User).where(
            User.provider == provider,
            User.provider_user_id == provider_user_id,
            User.deleted_at.is_(None),
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_email(self, email: str) -> User | None:
        stmt = select(User).where(User.email == email, User.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def create(
        self, provider: AuthProvider, provider_user_id: str, email: str, password_hash: str | None = None
    ) -> User:
        user = User(provider=provider, provider_user_id=provider_user_id, email=email, password_hash=password_hash)
        self.db.add(user)
        await self.db.flush()
        return user

    async def get_profile_by_user_id(self, user_id: UUID) -> UserProfile | None:
        stmt = select(UserProfile).where(UserProfile.user_id == user_id, UserProfile.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def get_profile_by_nickname(self, nickname: str) -> UserProfile | None:
        stmt = select(UserProfile).where(UserProfile.nickname == nickname, UserProfile.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def create_profile(
        self, user_id: UUID, nickname: str, profile_image_url: str | None, phone_number: str | None
    ) -> UserProfile:
        profile = UserProfile(
            user_id=user_id,
            nickname=nickname,
            profile_image_url=profile_image_url,
            phone_number=phone_number,
        )
        self.db.add(profile)
        await self.db.flush()
        return profile

    async def list_for_admin(self, query: str | None, limit: int, offset: int) -> list[User]:
        stmt = select(User).options(selectinload(User.profile)).where(User.deleted_at.is_(None))
        if query:
            like_pattern = f"%{query}%"
            stmt = stmt.outerjoin(UserProfile, UserProfile.user_id == User.id).where(
                or_(User.email.ilike(like_pattern), UserProfile.nickname.ilike(like_pattern))
            )
        stmt = stmt.order_by(User.created_at.desc()).limit(limit).offset(offset)
        result = await self.db.execute(stmt)
        return list(result.scalars().unique().all())
