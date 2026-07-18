import uuid

from sqlalchemy import ForeignKey, Index, String, Text, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class UserProfile(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """User-facing profile information, created during Profile Setup on first login."""

    __tablename__ = "UserProfile"
    __table_args__ = (
        UniqueConstraint("user_id", name="uq_userprofile_user_id"),
        UniqueConstraint("nickname", name="uq_userprofile_nickname"),
        Index("ix_userprofile_user_id", "user_id"),
        Index("ix_userprofile_nickname", "nickname"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    nickname: Mapped[str] = mapped_column(String(30), nullable=False)
    profile_image_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    phone_number: Mapped[str | None] = mapped_column(String(20), nullable=True)
    bio: Mapped[str | None] = mapped_column(Text, nullable=True)
