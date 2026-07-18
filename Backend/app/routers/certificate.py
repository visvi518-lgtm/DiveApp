from uuid import UUID

from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import TokenPayload, get_current_token_payload
from app.database.session import get_db
from app.schemas.certificate import CertificateCreateRequest, CertificateResponse, CertificateUpdateRequest
from app.services.certificate_service import CertificateService

router = APIRouter(prefix="/api/v1/certificates", tags=["certificates"])


@router.post("", response_model=CertificateResponse, status_code=status.HTTP_201_CREATED)
async def create_certificate(
    body: CertificateCreateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CertificateResponse:
    service = CertificateService(db)
    certificate = await service.create(token.user_id, body)
    return CertificateResponse.model_validate(certificate)


@router.get("", response_model=list[CertificateResponse])
async def list_certificates(
    token: TokenPayload = Depends(get_current_token_payload), db: AsyncSession = Depends(get_db)
) -> list[CertificateResponse]:
    service = CertificateService(db)
    certificates = await service.list(token.user_id)
    return [CertificateResponse.model_validate(certificate) for certificate in certificates]


@router.get("/{certificate_id}", response_model=CertificateResponse)
async def get_certificate(
    certificate_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CertificateResponse:
    service = CertificateService(db)
    certificate = await service.get(certificate_id, token.user_id)
    return CertificateResponse.model_validate(certificate)


@router.patch("/{certificate_id}", response_model=CertificateResponse)
async def update_certificate(
    certificate_id: UUID,
    body: CertificateUpdateRequest,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> CertificateResponse:
    service = CertificateService(db)
    certificate = await service.update(certificate_id, token.user_id, body)
    return CertificateResponse.model_validate(certificate)


@router.delete("/{certificate_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_certificate(
    certificate_id: UUID,
    token: TokenPayload = Depends(get_current_token_payload),
    db: AsyncSession = Depends(get_db),
) -> None:
    service = CertificateService(db)
    await service.delete(certificate_id, token.user_id)
