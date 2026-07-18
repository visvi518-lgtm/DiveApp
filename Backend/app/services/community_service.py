from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ForbiddenException, NotFoundException
from app.models.community_comment import CommunityComment
from app.models.community_post import CommunityPost
from app.models.enums import UserRole
from app.repositories.admin_log_repository import AdminLogRepository
from app.repositories.community_repository import CommunityCommentRepository, CommunityPostRepository


class CommunityPostService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.post_repository = CommunityPostRepository(db)
        self.admin_log_repository = AdminLogRepository(db)

    async def create(self, user_id: UUID, title: str, content: str) -> CommunityPost:
        post = await self.post_repository.create(user_id, title, content)
        await self.db.commit()
        return await self._get_or_raise(post.id)

    async def get(self, post_id: UUID) -> CommunityPost:
        post = await self._get_or_raise(post_id)
        await self.post_repository.increment_view_count(post)
        await self.db.commit()
        return await self._get_or_raise(post_id)

    async def list(self, query: str | None, author_id: UUID | None, limit: int, offset: int) -> list[CommunityPost]:
        return await self.post_repository.list(query, author_id, limit, offset)

    async def update(self, post_id: UUID, user_id: UUID, title: str | None, content: str | None) -> CommunityPost:
        post = await self._get_or_raise(post_id)
        self._ensure_author(post, user_id)
        if title is not None:
            post.title = title
        if content is not None:
            post.content = content
        await self.db.commit()
        return await self._get_or_raise(post_id)

    async def delete(self, post_id: UUID, user_id: UUID, user_role: UserRole) -> None:
        post = await self._get_or_raise(post_id)
        is_admin_action = user_role == UserRole.ADMIN and post.user_id != user_id
        if not is_admin_action:
            self._ensure_author(post, user_id)
        await self.post_repository.soft_delete(post)
        if is_admin_action:
            await self.admin_log_repository.record(user_id, "DELETE_POST", "CommunityPost", post_id)
        await self.db.commit()

    @staticmethod
    def _ensure_author(post: CommunityPost, user_id: UUID) -> None:
        if post.user_id != user_id:
            raise ForbiddenException("Only the author can modify this post")

    async def _get_or_raise(self, post_id: UUID) -> CommunityPost:
        post = await self.post_repository.get_by_id(post_id)
        if post is None:
            raise NotFoundException("Post not found")
        return post


class CommunityCommentService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.comment_repository = CommunityCommentRepository(db)
        self.post_repository = CommunityPostRepository(db)
        self.admin_log_repository = AdminLogRepository(db)

    async def create(self, post_id: UUID, user_id: UUID, content: str) -> CommunityComment:
        post = await self.post_repository.get_by_id(post_id)
        if post is None:
            raise NotFoundException("Post not found")

        comment = await self.comment_repository.create(post_id, user_id, content)
        post.comment_count += 1
        await self.db.commit()
        return await self._get_or_raise(comment.id)

    async def list_for_post(self, post_id: UUID) -> list[CommunityComment]:
        return await self.comment_repository.list_for_post(post_id)

    async def update(self, comment_id: UUID, user_id: UUID, content: str) -> CommunityComment:
        comment = await self._get_or_raise(comment_id)
        self._ensure_author(comment, user_id)
        comment.content = content
        await self.db.commit()
        return await self._get_or_raise(comment_id)

    async def delete(self, comment_id: UUID, user_id: UUID, user_role: UserRole) -> None:
        comment = await self._get_or_raise(comment_id)
        is_admin_action = user_role == UserRole.ADMIN and comment.user_id != user_id
        if not is_admin_action:
            self._ensure_author(comment, user_id)

        post = await self.post_repository.get_by_id(comment.post_id)
        await self.comment_repository.soft_delete(comment)
        if post is not None and post.comment_count > 0:
            post.comment_count -= 1
        if is_admin_action:
            await self.admin_log_repository.record(user_id, "DELETE_COMMENT", "CommunityComment", comment_id)
        await self.db.commit()

    @staticmethod
    def _ensure_author(comment: CommunityComment, user_id: UUID) -> None:
        if comment.user_id != user_id:
            raise ForbiddenException("Only the author can modify this comment")

    async def _get_or_raise(self, comment_id: UUID) -> CommunityComment:
        comment = await self.comment_repository.get_by_id(comment_id)
        if comment is None:
            raise NotFoundException("Comment not found")
        return comment
