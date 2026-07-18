from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import NotFoundException
from app.models.certificate import Certificate
from app.repositories.certificate_repository import CertificateRepository
from app.schemas.certificate import CertificateCreateRequest, CertificateUpdateRequest


class CertificateService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.certificate_repository = CertificateRepository(db)

    async def create(self, user_id: UUID, body: CertificateCreateRequest) -> Certificate:
        certificate = await self.certificate_repository.create(user_id, **body.model_dump())
        await self.db.commit()
        await self.db.refresh(certificate)
        return certificate

    async def get(self, certificate_id: UUID, user_id: UUID) -> Certificate:
        return await self._get_or_raise(certificate_id, user_id)

    async def list(self, user_id: UUID) -> list[Certificate]:
        return await self.certificate_repository.list_for_user(user_id)

    async def update(self, certificate_id: UUID, user_id: UUID, body: CertificateUpdateRequest) -> Certificate:
        certificate = await self._get_or_raise(certificate_id, user_id)
        for field, value in body.model_dump(exclude_unset=True).items():
            setattr(certificate, field, value)
        await self.db.commit()
        await self.db.refresh(certificate)
        return certificate

    async def delete(self, certificate_id: UUID, user_id: UUID) -> None:
        certificate = await self._get_or_raise(certificate_id, user_id)
        certificate.deleted_at = datetime.now(timezone.utc)
        await self.db.commit()

    async def _get_or_raise(self, certificate_id: UUID, user_id: UUID) -> Certificate:
        certificate = await self.certificate_repository.get_by_id(certificate_id, user_id)
        if certificate is None:
            raise NotFoundException("Certificate not found")
        return certificate
