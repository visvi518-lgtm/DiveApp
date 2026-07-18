import uuid

from sqlalchemy import Boolean, CheckConstraint, ForeignKey, Index, Integer, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.user import User


class CommunityPost(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Community board post. MVP operates a single board; category expansion is future work."""

    __tablename__ = "CommunityPost"
    __table_args__ = (
        CheckConstraint("view_count >= 0", name="ck_communitypost_view_count_nonnegative"),
        CheckConstraint("like_count >= 0", name="ck_communitypost_like_count_nonnegative"),
        CheckConstraint("comment_count >= 0", name="ck_communitypost_comment_count_nonnegative"),
        Index("ix_communitypost_user_id", "user_id"),
        Index("ix_communitypost_is_pinned", "is_pinned"),
        Index("ix_communitypost_created_at", "created_at"),
        Index("ix_communitypost_view_count", "view_count"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    view_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    like_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    comment_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    is_pinned: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)

    author: Mapped[User] = relationship(User)
