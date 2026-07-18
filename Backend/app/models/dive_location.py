from sqlalchemy import CheckConstraint, Index, Numeric, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin, UUIDPrimaryKeyMixin


class DiveLocation(UUIDPrimaryKeyMixin, TimestampMixin, Base):
    """Reusable dive site location, shared across dive logs and users."""

    __tablename__ = "DiveLocation"
    __table_args__ = (
        UniqueConstraint("name", "city", "country", name="uq_divelocation_name_city_country"),
        CheckConstraint("latitude BETWEEN -90 AND 90", name="ck_divelocation_latitude_range"),
        CheckConstraint("longitude BETWEEN -180 AND 180", name="ck_divelocation_longitude_range"),
        Index("ix_divelocation_name", "name"),
        Index("ix_divelocation_city", "city"),
        Index("ix_divelocation_lat_lng", "latitude", "longitude"),
    )

    name: Mapped[str] = mapped_column(String(100), nullable=False)
    address: Mapped[str | None] = mapped_column(String(255), nullable=True)
    latitude: Mapped[float] = mapped_column(Numeric(10, 7), nullable=False)
    longitude: Mapped[float] = mapped_column(Numeric(10, 7), nullable=False)
    naver_place_id: Mapped[str | None] = mapped_column(String(100), nullable=True)
    country: Mapped[str | None] = mapped_column(String(100), nullable=True)
    city: Mapped[str | None] = mapped_column(String(100), nullable=True)
