from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class InformationArticleCreateRequest(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    content: str = Field(min_length=1)
    thumbnail_image_url: str | None = None
    is_published: bool = False


class InformationArticleUpdateRequest(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=200)
    content: str | None = Field(default=None, min_length=1)
    thumbnail_image_url: str | None = None
    is_published: bool | None = None


class InformationArticleResponse(BaseModel):
    id: UUID
    title: str
    content: str
    thumbnail_image_url: str | None
    view_count: int
    is_published: bool
    published_at: datetime | None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class InformationArticleListItem(BaseModel):
    id: UUID
    title: str
    thumbnail_image_url: str | None
    published_at: datetime | None

    model_config = {"from_attributes": True}
