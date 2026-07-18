from uuid import UUID

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, require_admin
from app.database.session import get_db
from app.schemas.information import (
    InformationArticleCreateRequest,
    InformationArticleListItem,
    InformationArticleResponse,
    InformationArticleUpdateRequest,
)
from app.services.information_service import InformationArticleService

router = APIRouter(prefix="/api/v1/information/articles", tags=["information"])
admin_router = APIRouter(prefix="/api/v1/admin/information/articles", tags=["admin-information"])


@router.get("", response_model=list[InformationArticleListItem])
async def list_articles(
    q: str | None = None,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    db: AsyncSession = Depends(get_db),
) -> list[InformationArticleListItem]:
    service = InformationArticleService(db)
    articles = await service.list_published(q, limit, offset)
    return [InformationArticleListItem.model_validate(article) for article in articles]


@router.get("/{article_id}", response_model=InformationArticleResponse)
async def get_article(article_id: UUID, db: AsyncSession = Depends(get_db)) -> InformationArticleResponse:
    service = InformationArticleService(db)
    article = await service.get_published(article_id)
    return InformationArticleResponse.model_validate(article)


@admin_router.post("", response_model=InformationArticleResponse, status_code=status.HTTP_201_CREATED)
async def create_article(
    body: InformationArticleCreateRequest,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> InformationArticleResponse:
    service = InformationArticleService(db)
    article = await service.create(token.user_id, body)
    return InformationArticleResponse.model_validate(article)


@admin_router.get("", response_model=list[InformationArticleResponse])
async def list_articles_admin(
    q: str | None = None,
    limit: int = Query(default=20, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> list[InformationArticleResponse]:
    service = InformationArticleService(db)
    articles = await service.list_for_admin(q, limit, offset)
    return [InformationArticleResponse.model_validate(article) for article in articles]


@admin_router.patch("/{article_id}", response_model=InformationArticleResponse)
async def update_article(
    article_id: UUID,
    body: InformationArticleUpdateRequest,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> InformationArticleResponse:
    service = InformationArticleService(db)
    article = await service.update(token.user_id, article_id, body)
    return InformationArticleResponse.model_validate(article)


@admin_router.delete("/{article_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_article(
    article_id: UUID,
    token: TokenPayload = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
) -> None:
    service = InformationArticleService(db)
    await service.delete(token.user_id, article_id)
