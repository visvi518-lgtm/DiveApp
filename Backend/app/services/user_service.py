from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ConflictException, NotFoundException
from app.models.user import User
from app.models.user_profile import UserProfile
from app.repositories.user_repository import UserRepository


class UserService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.user_repository = UserRepository(db)

    async def get_current_user(self, user_id: UUID) -> User:
        user = await self.user_repository.get_by_id(user_id)
        if user is None:
            raise NotFoundException("User not found")
        return user

    async def setup_profile(
        self, user_id: UUID, nickname: str, profile_image_url: str | None, phone_number: str | None
    ) -> UserProfile:
        existing_profile = await self.user_repository.get_profile_by_user_id(user_id)
        if existing_profile is not None:
            raise ConflictException("Profile already exists")

        nickname_owner = await self.user_repository.get_profile_by_nickname(nickname)
        if nickname_owner is not None:
            raise ConflictException("Nickname already in use")

        profile = await self.user_repository.create_profile(
            user_id=user_id,
            nickname=nickname,
            profile_image_url=profile_image_url,
            phone_number=phone_number,
        )
        await self.db.commit()
        await self.db.refresh(profile)
        return profile
