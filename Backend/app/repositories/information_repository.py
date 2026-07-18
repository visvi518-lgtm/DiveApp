from uuid import UUID

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.information_article import InformationArticle


class InformationArticleRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, **fields) -> InformationArticle:
        article = InformationArticle(**fields)
        self.db.add(article)
        await self.db.flush()
        return article

    async def get_by_id(self, article_id: UUID) -> InformationArticle | None:
        stmt = select(InformationArticle).where(
            InformationArticle.id == article_id, InformationArticle.deleted_at.is_(None)
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list(
        self, query: str | None, published_only: bool, limit: int, offset: int
    ) -> list[InformationArticle]:
        stmt = select(InformationArticle).where(InformationArticle.deleted_at.is_(None))
        if published_only:
            stmt = stmt.where(InformationArticle.is_published.is_(True))
        if query:
            like_pattern = f"%{query}%"
            stmt = stmt.where(
                or_(InformationArticle.title.ilike(like_pattern), InformationArticle.content.ilike(like_pattern))
            )
        stmt = stmt.order_by(InformationArticle.published_at.desc().nulls_last(), InformationArticle.created_at.desc())
        stmt = stmt.limit(limit).offset(offset)
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def increment_view_count(self, article: InformationArticle) -> None:
        article.view_count += 1
