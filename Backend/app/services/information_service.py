from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundException
from app.models.information_article import InformationArticle
from app.repositories.admin_log_repository import AdminLogRepository
from app.repositories.information_repository import InformationArticleRepository
from app.schemas.information import InformationArticleCreateRequest, InformationArticleUpdateRequest


class InformationArticleService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.article_repository = InformationArticleRepository(db)
        self.admin_log_repository = AdminLogRepository(db)

    async def create(self, admin_user_id: UUID, body: InformationArticleCreateRequest) -> InformationArticle:
        fields = body.model_dump()
        if fields["is_published"]:
            fields["published_at"] = datetime.now(timezone.utc)
        article = await self.article_repository.create(**fields)
        await self.admin_log_repository.record(admin_user_id, "CREATE_ARTICLE", "InformationArticle", article.id)
        await self.db.commit()
        await self.db.refresh(article)
        return article

    async def get_published(self, article_id: UUID) -> InformationArticle:
        article = await self._get_or_raise(article_id)
        if not article.is_published:
            raise NotFoundException("Article not found")
        await self.article_repository.increment_view_count(article)
        await self.db.commit()
        await self.db.refresh(article)
        return article

    async def get_for_admin(self, article_id: UUID) -> InformationArticle:
        return await self._get_or_raise(article_id)

    async def list_published(self, query: str | None, limit: int, offset: int) -> list[InformationArticle]:
        return await self.article_repository.list(query, published_only=True, limit=limit, offset=offset)

    async def list_for_admin(self, query: str | None, limit: int, offset: int) -> list[InformationArticle]:
        return await self.article_repository.list(query, published_only=False, limit=limit, offset=offset)

    async def update(
        self, admin_user_id: UUID, article_id: UUID, body: InformationArticleUpdateRequest
    ) -> InformationArticle:
        article = await self._get_or_raise(article_id)
        updates = body.model_dump(exclude_unset=True)
        was_published = article.is_published
        for field, value in updates.items():
            setattr(article, field, value)
        if article.is_published and not was_published:
            article.published_at = datetime.now(timezone.utc)
        await self.admin_log_repository.record(admin_user_id, "UPDATE_ARTICLE", "InformationArticle", article_id)
        await self.db.commit()
        await self.db.refresh(article)
        return article

    async def delete(self, admin_user_id: UUID, article_id: UUID) -> None:
        article = await self._get_or_raise(article_id)
        article.deleted_at = datetime.now(timezone.utc)
        await self.admin_log_repository.record(admin_user_id, "DELETE_ARTICLE", "InformationArticle", article_id)
        await self.db.commit()

    async def _get_or_raise(self, article_id: UUID) -> InformationArticle:
        article = await self.article_repository.get_by_id(article_id)
        if article is None:
            raise NotFoundException("Article not found")
        return article
