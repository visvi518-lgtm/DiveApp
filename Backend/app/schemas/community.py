from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class AuthorResponse(BaseModel):
    id: UUID
    nickname: str | None


class CommunityPostCreateRequest(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    content: str = Field(min_length=1)


class CommunityPostUpdateRequest(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=200)
    content: str | None = Field(default=None, min_length=1)


class CommunityPostResponse(BaseModel):
    id: UUID
    title: str
    content: str
    author: AuthorResponse
    view_count: int
    like_count: int
    comment_count: int
    is_pinned: bool
    created_at: datetime
    updated_at: datetime


class CommunityPostListItem(BaseModel):
    id: UUID
    title: str
    author: AuthorResponse
    view_count: int
    comment_count: int
    is_pinned: bool
    created_at: datetime


class CommunityCommentCreateRequest(BaseModel):
    content: str = Field(min_length=1)


class CommunityCommentUpdateRequest(BaseModel):
    content: str = Field(min_length=1)


class CommunityCommentResponse(BaseModel):
    id: UUID
    content: str
    author: AuthorResponse
    created_at: datetime
    updated_at: datetime
