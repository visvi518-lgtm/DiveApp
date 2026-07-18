from uuid import UUID

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.certificate import Certificate


class CertificateRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, user_id: UUID, **fields) -> Certificate:
        certificate = Certificate(user_id=user_id, **fields)
        self.db.add(certificate)
        await self.db.flush()
        return certificate

    async def get_by_id(self, certificate_id: UUID, user_id: UUID) -> Certificate | None:
        stmt = select(Certificate).where(
            Certificate.id == certificate_id,
            Certificate.user_id == user_id,
            Certificate.deleted_at.is_(None),
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(self, user_id: UUID) -> list[Certificate]:
        stmt = (
            select(Certificate)
            .where(Certificate.user_id == user_id, Certificate.deleted_at.is_(None))
            .order_by(Certificate.created_at.desc())
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().all())
