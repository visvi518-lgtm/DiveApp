from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field, model_validator

from app.models.enums import BannerType


class BannerCreateRequest(BaseModel):
    title: str = Field(max_length=100)
    image_url: str
    banner_type: BannerType
    target_url: str | None = None
    display_order: int = Field(default=1, ge=1)
    is_active: bool = True
    start_at: datetime | None = None
    end_at: datetime | None = None

    @model_validator(mode="after")
    def check_date_range(self) -> "BannerCreateRequest":
        if self.start_at is not None and self.end_at is not None and self.start_at > self.end_at:
            raise ValueError("start_at must be before end_at")
        return self


class BannerUpdateRequest(BaseModel):
    title: str | None = Field(default=None, max_length=100)
    image_url: str | None = None
    banner_type: BannerType | None = None
    target_url: str | None = None
    display_order: int | None = Field(default=None, ge=1)
    is_active: bool | None = None
    start_at: datetime | None = None
    end_at: datetime | None = None


class BannerResponse(BaseModel):
    id: UUID
    title: str
    image_url: str
    banner_type: BannerType
    target_url: str | None
    display_order: int
    is_active: bool
    start_at: datetime | None
    end_at: datetime | None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}
