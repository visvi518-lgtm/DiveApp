import uuid

from sqlalchemy import CheckConstraint, ForeignKey, Index, Integer, Numeric, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class FreedivingLog(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Freediving-specific data, created only when DiveLog.dive_type is FREEDIVING."""

    __tablename__ = "FreedivingLog"
    __table_args__ = (
        UniqueConstraint("dive_log_id", name="uq_freedivinglog_dive_log_id"),
        CheckConstraint("max_depth >= 0", name="ck_freedivinglog_max_depth_nonnegative"),
        CheckConstraint("dive_time_seconds >= 0", name="ck_freedivinglog_dive_time_nonnegative"),
        Index("ix_freedivinglog_dive_log_id", "dive_log_id"),
        Index("ix_freedivinglog_max_depth", "max_depth"),
    )

    dive_log_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("DiveLog.id"), nullable=False)
    max_depth: Mapped[float] = mapped_column(Numeric(5, 2), nullable=False)
    dive_time_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
