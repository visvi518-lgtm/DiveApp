import uuid
from datetime import datetime

from sqlalchemy import Boolean, CheckConstraint, DateTime, ForeignKey, Index, Integer, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class TrainingRecord(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """CO2 Table training program settings and completion result."""

    __tablename__ = "TrainingRecord"
    __table_args__ = (
        CheckConstraint("total_sets >= 5 AND total_sets <= 20", name="ck_trainingrecord_total_sets_range"),
        CheckConstraint("completed_sets >= 0 AND completed_sets <= total_sets", name="ck_trainingrecord_completed_sets_range"),
        CheckConstraint("rest_time_seconds > 0", name="ck_trainingrecord_rest_time_positive"),
        CheckConstraint("hold_time_seconds > 0", name="ck_trainingrecord_hold_time_positive"),
        Index("ix_trainingrecord_user_id", "user_id"),
        Index("ix_trainingrecord_completed_at", "completed_at"),
        Index("ix_trainingrecord_is_completed", "is_completed"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    total_sets: Mapped[int] = mapped_column(Integer, nullable=False)
    completed_sets: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    is_completed: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    rest_time_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
    hold_time_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
    rest_interval_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
    hold_interval_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
    completed_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
