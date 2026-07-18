import uuid

from sqlalchemy import CheckConstraint, ForeignKey, Index, Integer, Text, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class DivePhoto(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Photos attached to a dive log, ordered by display_order."""

    __tablename__ = "DivePhoto"
    __table_args__ = (
        UniqueConstraint("dive_log_id", "display_order", name="uq_divephoto_log_display_order"),
        CheckConstraint("display_order >= 1", name="ck_divephoto_display_order_positive"),
        Index("ix_divephoto_dive_log_id", "dive_log_id"),
        Index("ix_divephoto_display_order", "display_order"),
    )

    dive_log_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("DiveLog.id"), nullable=False)
    image_url: Mapped[str] = mapped_column(Text, nullable=False)
    display_order: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
