from datetime import datetime

from sqlalchemy import Boolean, DateTime, Enum, Index, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.enums import AccountStatus, AuthProvider, UserRole
from app.models.user_profile import UserProfile


class User(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Authentication and account identity. Profile data lives in UserProfile."""

    __tablename__ = "User"
    __table_args__ = (
        UniqueConstraint("email", name="uq_user_email"),
        UniqueConstraint("provider", "provider_user_id", name="uq_user_provider_identity"),
        Index("ix_user_email", "email"),
        Index("ix_user_provider_identity", "provider", "provider_user_id"),
        Index("ix_user_account_status", "account_status"),
    )

    provider: Mapped[AuthProvider] = mapped_column(Enum(AuthProvider, name="auth_provider"), nullable=False)
    provider_user_id: Mapped[str] = mapped_column(String(255), nullable=False)
    email: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[UserRole] = mapped_column(Enum(UserRole, name="user_role"), nullable=False, default=UserRole.USER)
    account_status: Mapped[AccountStatus] = mapped_column(
        Enum(AccountStatus, name="account_status"), nullable=False, default=AccountStatus.ACTIVE
    )
    last_login_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    email_verified: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    password_hash: Mapped[str | None] = mapped_column(String(255), nullable=True)
    """Only set for provider=EMAIL accounts; NULL for social-login accounts."""

    profile: Mapped[UserProfile | None] = relationship(UserProfile, uselist=False)
