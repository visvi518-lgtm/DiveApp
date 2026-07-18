from datetime import datetime

from sqlalchemy import Boolean, CheckConstraint, DateTime, Enum, Index, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.enums import BannerType


class Banner(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Promotional banner shown on the home screen and key screens."""

    __tablename__ = "Banner"
    __table_args__ = (
        CheckConstraint("display_order >= 1", name="ck_banner_display_order_positive"),
        Index("ix_banner_is_active", "is_active"),
        Index("ix_banner_display_order", "display_order"),
        Index("ix_banner_start_at", "start_at"),
        Index("ix_banner_end_at", "end_at"),
    )

    title: Mapped[str] = mapped_column(String(100), nullable=False)
    image_url: Mapped[str] = mapped_column(Text, nullable=False)
    banner_type: Mapped[BannerType] = mapped_column(Enum(BannerType, name="banner_type"), nullable=False)
    target_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    display_order: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    start_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    end_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
