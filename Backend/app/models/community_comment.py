import uuid

from sqlalchemy import ForeignKey, Index, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.user import User


class CommunityComment(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Comment on a community post. Reply threads are future work (parent_comment_id)."""

    __tablename__ = "CommunityComment"
    __table_args__ = (
        Index("ix_communitycomment_post_id", "post_id"),
        Index("ix_communitycomment_created_at", "created_at"),
    )

    post_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("CommunityPost.id"), nullable=False)
    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)

    author: Mapped[User] = relationship(User)
