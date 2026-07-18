from datetime import date

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.community_comment import CommunityComment
from app.models.community_post import CommunityPost
from app.models.dive_log import DiveLog
from app.models.enums import AccountStatus
from app.models.user import User


class AdminDashboardRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_dashboard_stats(self, today: date) -> dict:
        total_user_count = (
            await self.db.execute(
                select(func.count()).select_from(select(User).where(User.deleted_at.is_(None)).subquery())
            )
        ).scalar_one()

        new_user_count_today = (
            await self.db.execute(
                select(func.count()).select_from(
                    select(User)
                    .where(User.deleted_at.is_(None), func.date(User.created_at) == today)
                    .subquery()
                )
            )
        ).scalar_one()

        active_user_count = (
            await self.db.execute(
                select(func.count()).select_from(
                    select(User)
                    .where(User.deleted_at.is_(None), User.account_status == AccountStatus.ACTIVE)
                    .subquery()
                )
            )
        ).scalar_one()

        post_count = (
            await self.db.execute(
                select(func.count()).select_from(
                    select(CommunityPost).where(CommunityPost.deleted_at.is_(None)).subquery()
                )
            )
        ).scalar_one()

        comment_count = (
            await self.db.execute(
                select(func.count()).select_from(
                    select(CommunityComment).where(CommunityComment.deleted_at.is_(None)).subquery()
                )
            )
        ).scalar_one()

        dive_log_count = (
            await self.db.execute(
                select(func.count()).select_from(select(DiveLog).where(DiveLog.deleted_at.is_(None)).subquery())
            )
        ).scalar_one()

        return {
            "total_user_count": total_user_count,
            "new_user_count_today": new_user_count_today,
            "active_user_count": active_user_count,
            "post_count": post_count,
            "comment_count": comment_count,
            "dive_log_count": dive_log_count,
        }
