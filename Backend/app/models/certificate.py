import uuid
from datetime import date

from sqlalchemy import CheckConstraint, Enum, ForeignKey, Index, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.enums import CertificationOrganization


class Certificate(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """A dive certification owned by a user."""

    __tablename__ = "Certificate"
    __table_args__ = (
        CheckConstraint(
            "expiration_date IS NULL OR issue_date IS NULL OR expiration_date >= issue_date",
            name="ck_certificate_expiration_after_issue",
        ),
        Index("ix_certificate_user_id", "user_id"),
        Index("ix_certificate_organization", "organization"),
        Index("ix_certificate_certification_level", "certification_level"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    organization: Mapped[CertificationOrganization] = mapped_column(
        Enum(CertificationOrganization, name="certification_organization"), nullable=False
    )
    certification_level: Mapped[str] = mapped_column(String(100), nullable=False)
    certification_number: Mapped[str | None] = mapped_column(String(100), nullable=True)
    issue_date: Mapped[date | None] = mapped_column(nullable=True)
    expiration_date: Mapped[date | None] = mapped_column(nullable=True)
    instructor: Mapped[str | None] = mapped_column(String(100), nullable=True)
    dive_center: Mapped[str | None] = mapped_column(String(100), nullable=True)
    certificate_image_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    memo: Mapped[str | None] = mapped_column(Text, nullable=True)
