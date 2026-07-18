from datetime import datetime

from sqlalchemy import Boolean, CheckConstraint, DateTime, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class InformationArticle(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Admin-authored informational content for freedivers and scuba divers."""

    __tablename__ = "InformationArticle"
    __table_args__ = (
        CheckConstraint("view_count >= 0", name="ck_informationarticle_view_count_nonnegative"),
        Index("ix_informationarticle_is_published", "is_published"),
        Index("ix_informationarticle_published_at", "published_at"),
        Index("ix_informationarticle_created_at", "created_at"),
    )

    title: Mapped[str] = mapped_column(String(200), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    thumbnail_image_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    view_count: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    is_published: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    published_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
