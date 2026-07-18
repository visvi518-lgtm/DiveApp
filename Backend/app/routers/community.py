from uuid import UUID

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.models.community_comment import CommunityComment
from app.models.community_post import CommunityPost
from app.schemas.community import (
    AuthorResponse,
    CommunityCommentCreateRequest,
    CommunityCommentResponse,
    CommunityCommentUpdateRequest,
    CommunityPostCreateRequest,
    CommunityPostListItem,
    CommunityPostResponse,
    CommunityPostUpdateRequest,
)
from app.services.community_service import CommunityCommentService, CommunityPostService
from app.services.user_service import UserService

router = APIRouter(prefix="/api/v1/community", tags=["community"])


def _author_response(author) -> AuthorResponse:
    return AuthorResponse(id=author.id, nickname=author.profile.nickname if author.profile else None)


def _post_response(post: CommunityPost) -> CommunityPostResponse:
    return CommunityPostResponse(
        id=post.id,
        title=post.title,
        content=post.content,
        author=_author_response(post.author),
        view_count=post.view_count,
        like_count=post.like_count,
        comment_count=post.comment_count,
        is_pinned=post.is_pinned,
        created_at=post.created_at,
        updated_at=post.updated_at,
    )


def _post_list_item(post: CommunityPost) -> CommunityPostListItem:
    return CommunityPostListItem(
        id=post.id,
        title=post.title,
        author=_author_response(post.author),
        view_count=post.view_count,
        comment_count=post.comment_count,
        is_pinned=post.is_pinned,
        created_at=post.created_at,
    )


def _comment_response(comment: CommunityComment) -> CommunityCommentResponse:
    return CommunityCommentResponse(
        id=comment.id,
        content=comment.content,
        author=_author_response(comment.author),
        created_at=comment.created_at,
        updated_at=comment.updated_at,
    )


@router.post("/posts", response_model=CommunityPostResponse, status_code=status.HTTP_201_CREATED)
async def create_post(
    body: CommunityPostCreateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CommunityPostResponse:
    service = CommunityPostService(db)
    post = await service.create(token.user_id, body.title, body.content)
    return _post_response(post)


@router.get("/posts", response_model=list[CommunityPostListItem])
async def list_posts(
    q: str | None = None,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    db: AsyncSession = Depends(get_db),
) -> list[CommunityPostListItem]:
    service = CommunityPostService(db)
    posts = await service.list(q, None, limit, offset)
    return [_post_list_item(post) for post in posts]


@router.get("/posts/mine", response_model=list[CommunityPostListItem])
async def list_my_posts(
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> list[CommunityPostListItem]:
    service = CommunityPostService(db)
    posts = await service.list(None, token.user_id, limit, offset)
    return [_post_list_item(post) for post in posts]


@router.get("/posts/{post_id}", response_model=CommunityPostResponse)
async def get_post(post_id: UUID, db: AsyncSession = Depends(get_db)) -> CommunityPostResponse:
    service = CommunityPostService(db)
    post = await service.get(post_id)
    return _post_response(post)


@router.patch("/posts/{post_id}", response_model=CommunityPostResponse)
async def update_post(
    post_id: UUID,
    body: CommunityPostUpdateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CommunityPostResponse:
    service = CommunityPostService(db)
    post = await service.update(post_id, token.user_id, body.title, body.content)
    return _post_response(post)


@router.delete("/posts/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_post(
    post_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> None:
    user_service = UserService(db)
    user = await user_service.get_current_user(token.user_id)
    service = CommunityPostService(db)
    await service.delete(post_id, token.user_id, user.role)


@router.post("/posts/{post_id}/comments", response_model=CommunityCommentResponse, status_code=status.HTTP_201_CREATED)
async def create_comment(
    post_id: UUID,
    body: CommunityCommentCreateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CommunityCommentResponse:
    service = CommunityCommentService(db)
    comment = await service.create(post_id, token.user_id, body.content)
    return _comment_response(comment)


@router.get("/posts/{post_id}/comments", response_model=list[CommunityCommentResponse])
async def list_comments(post_id: UUID, db: AsyncSession = Depends(get_db)) -> list[CommunityCommentResponse]:
    service = CommunityCommentService(db)
    comments = await service.list_for_post(post_id)
    return [_comment_response(comment) for comment in comments]


@router.patch("/comments/{comment_id}", response_model=CommunityCommentResponse)
async def update_comment(
    comment_id: UUID,
    body: CommunityCommentUpdateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CommunityCommentResponse:
    service = CommunityCommentService(db)
    comment = await service.update(comment_id, token.user_id, body.content)
    return _comment_response(comment)


@router.delete("/comments/{comment_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_comment(
    comment_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> None:
    user_service = UserService(db)
    user = await user_service.get_current_user(token.user_id)
    service = CommunityCommentService(db)
    await service.delete(comment_id, token.user_id, user.role)
