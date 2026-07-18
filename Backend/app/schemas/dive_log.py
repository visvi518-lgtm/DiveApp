from datetime import date, datetime
from uuid import UUID

from pydantic import BaseModel, Field, model_validator

from app.models.enums import DiveType


class DiveLocationInput(BaseModel):
    name: str = Field(max_length=100)
    address: str | None = Field(default=None, max_length=255)
    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    naver_place_id: str | None = Field(default=None, max_length=100)
    country: str | None = Field(default=None, max_length=100)
    city: str | None = Field(default=None, max_length=100)


class DiveLocationResponse(BaseModel):
    id: UUID
    name: str
    address: str | None
    latitude: float
    longitude: float
    country: str | None
    city: str | None

    model_config = {"from_attributes": True}


class FreedivingDetailInput(BaseModel):
    max_depth: float = Field(ge=0)
    dive_time_seconds: int = Field(ge=0)


class ScubaDetailInput(BaseModel):
    max_depth: float = Field(ge=0)
    dive_time_seconds: int = Field(ge=0)
    tank_pressure_start: int = Field(ge=0)
    tank_pressure_end: int = Field(ge=0)

    @model_validator(mode="after")
    def check_pressure_order(self) -> "ScubaDetailInput":
        if self.tank_pressure_start < self.tank_pressure_end:
            raise ValueError("tank_pressure_start must be >= tank_pressure_end")
        return self


class FreedivingDetailResponse(BaseModel):
    max_depth: float
    dive_time_seconds: int

    model_config = {"from_attributes": True}


class ScubaDetailResponse(BaseModel):
    max_depth: float
    dive_time_seconds: int
    tank_pressure_start: int
    tank_pressure_end: int

    model_config = {"from_attributes": True}


class DivePhotoInput(BaseModel):
    image_url: str
    display_order: int = Field(default=1, ge=1)


class DivePhotoResponse(BaseModel):
    id: UUID
    image_url: str
    display_order: int

    model_config = {"from_attributes": True}


class DiveLogCreateRequest(BaseModel):
    dive_type: DiveType
    dive_date: date
    location: DiveLocationInput
    latitude: float | None = Field(default=None, ge=-90, le=90)
    longitude: float | None = Field(default=None, ge=-180, le=180)
    memo: str | None = None
    freediving: FreedivingDetailInput | None = None
    scuba: ScubaDetailInput | None = None
    photos: list[DivePhotoInput] = []

    @model_validator(mode="after")
    def check_detail_matches_type(self) -> "DiveLogCreateRequest":
        if self.dive_type == DiveType.FREEDIVING and self.freediving is None:
            raise ValueError("freediving detail is required when dive_type is FREEDIVING")
        if self.dive_type == DiveType.SCUBA and self.scuba is None:
            raise ValueError("scuba detail is required when dive_type is SCUBA")
        return self


class DiveLogUpdateRequest(BaseModel):
    memo: str | None = None
    freediving: FreedivingDetailInput | None = None
    scuba: ScubaDetailInput | None = None
    photos: list[DivePhotoInput] | None = None


class DiveLogResponse(BaseModel):
    id: UUID
    dive_type: DiveType
    dive_date: date
    location: DiveLocationResponse
    latitude: float | None
    longitude: float | None
    memo: str | None
    freediving: FreedivingDetailResponse | None
    scuba: ScubaDetailResponse | None
    photos: list[DivePhotoResponse]
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class DiveLogListItem(BaseModel):
    id: UUID
    dive_type: DiveType
    dive_date: date
    location_name: str
    cover_image_url: str | None
    max_depth: float


class DiveLogStatisticsResponse(BaseModel):
    total_dive_count: int
    max_depth_overall: float
    total_dive_time_seconds: int
    freediving_count: int
    scuba_count: int
