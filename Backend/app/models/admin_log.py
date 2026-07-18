import uuid

from sqlalchemy import DateTime, ForeignKey, Index, String, Text, func
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, UUIDPrimaryKeyMixin
from datetime import datetime


class AdminLog(UUIDPrimaryKeyMixin, Base):
    """Immutable audit trail of administrator actions. Never updated, only inserted."""

    __tablename__ = "AdminLog"
    __table_args__ = (
        Index("ix_adminlog_admin_user_id", "admin_user_id"),
        Index("ix_adminlog_action", "action"),
        Index("ix_adminlog_target_type", "target_type"),
        Index("ix_adminlog_created_at", "created_at"),
    )

    admin_user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    action: Mapped[str] = mapped_column(String(100), nullable=False)
    target_type: Mapped[str] = mapped_column(String(100), nullable=False)
    target_id: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True), nullable=True)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    ip_address: Mapped[str | None] = mapped_column(String(45), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
