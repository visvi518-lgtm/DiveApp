from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.community_comment import CommunityComment
from app.models.community_post import CommunityPost
from app.models.user import User


class CommunityPostRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    def _base_query(self):
        return select(CommunityPost).options(selectinload(CommunityPost.author).selectinload(User.profile))

    async def create(self, user_id: UUID, title: str, content: str) -> CommunityPost:
        post = CommunityPost(user_id=user_id, title=title, content=content)
        self.db.add(post)
        await self.db.flush()
        return post

    async def get_by_id(self, post_id: UUID) -> CommunityPost | None:
        stmt = self._base_query().where(CommunityPost.id == post_id, CommunityPost.deleted_at.is_(None))
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list(
        self, query: str | None, author_id: UUID | None, limit: int, offset: int
    ) -> list[CommunityPost]:
        stmt = self._base_query().where(CommunityPost.deleted_at.is_(None))
        if author_id is not None:
            stmt = stmt.where(CommunityPost.user_id == author_id)
        if query:
            like_pattern = f"%{query}%"
            stmt = stmt.where(or_(CommunityPost.title.ilike(like_pattern), CommunityPost.content.ilike(like_pattern)))
        stmt = stmt.order_by(CommunityPost.is_pinned.desc(), CommunityPost.created_at.desc())
        stmt = stmt.limit(limit).offset(offset)
        result = await self.db.execute(stmt)
        return list(result.scalars().unique().all())

    async def increment_view_count(self, post: CommunityPost) -> None:
        post.view_count += 1

    async def soft_delete(self, post: CommunityPost) -> None:
        post.deleted_at = datetime.now(timezone.utc)


class CommunityCommentRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, post_id: UUID, user_id: UUID, content: str) -> CommunityComment:
        comment = CommunityComment(post_id=post_id, user_id=user_id, content=content)
        self.db.add(comment)
        await self.db.flush()
        return comment

    async def get_by_id(self, comment_id: UUID) -> CommunityComment | None:
        stmt = (
            select(CommunityComment)
            .options(selectinload(CommunityComment.author).selectinload(User.profile))
            .where(CommunityComment.id == comment_id, CommunityComment.deleted_at.is_(None))
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_post(self, post_id: UUID) -> list[CommunityComment]:
        stmt = (
            select(CommunityComment)
            .options(selectinload(CommunityComment.author).selectinload(User.profile))
            .where(CommunityComment.post_id == post_id, CommunityComment.deleted_at.is_(None))
            .order_by(CommunityComment.created_at.asc())
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().unique().all())

    async def soft_delete(self, comment: CommunityComment) -> None:
        comment.deleted_at = datetime.now(timezone.utc)
