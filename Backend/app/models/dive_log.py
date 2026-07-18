import uuid
from datetime import date

from sqlalchemy import CheckConstraint, Enum, ForeignKey, Index, Numeric, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin
from app.models.dive_location import DiveLocation
from app.models.dive_photo import DivePhoto
from app.models.enums import DiveType
from app.models.freediving_log import FreedivingLog
from app.models.scuba_log import ScubaLog


class DiveLog(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Common dive information shared by freediving and scuba logs."""

    __tablename__ = "DiveLog"
    __table_args__ = (
        CheckConstraint("latitude IS NULL OR latitude BETWEEN -90 AND 90", name="ck_divelog_latitude_range"),
        CheckConstraint("longitude IS NULL OR longitude BETWEEN -180 AND 180", name="ck_divelog_longitude_range"),
        Index("ix_divelog_user_id", "user_id"),
        Index("ix_divelog_location_id", "location_id"),
        Index("ix_divelog_dive_date", "dive_date"),
        Index("ix_divelog_dive_type", "dive_type"),
        Index("ix_divelog_user_id_dive_date", "user_id", "dive_date"),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("User.id"), nullable=False)
    location_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("DiveLocation.id"), nullable=False
    )
    dive_type: Mapped[DiveType] = mapped_column(Enum(DiveType, name="dive_type"), nullable=False)
    dive_date: Mapped[date] = mapped_column(nullable=False)
    latitude: Mapped[float | None] = mapped_column(Numeric(10, 7), nullable=True)
    longitude: Mapped[float | None] = mapped_column(Numeric(10, 7), nullable=True)
    memo: Mapped[str | None] = mapped_column(Text, nullable=True)

    location: Mapped[DiveLocation] = relationship(DiveLocation)
    freediving: Mapped[FreedivingLog | None] = relationship(FreedivingLog, uselist=False)
    scuba: Mapped[ScubaLog | None] = relationship(ScubaLog, uselist=False)
    photos: Mapped[list[DivePhoto]] = relationship(
        DivePhoto, order_by=DivePhoto.display_order, viewonly=False
    )
