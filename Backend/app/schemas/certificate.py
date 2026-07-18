from datetime import date, datetime
from uuid import UUID

from pydantic import BaseModel, Field, model_validator

from app.models.enums import CertificationOrganization


class CertificateCreateRequest(BaseModel):
    organization: CertificationOrganization
    certification_level: str = Field(max_length=100)
    certification_number: str | None = Field(default=None, max_length=100)
    issue_date: date | None = None
    expiration_date: date | None = None
    instructor: str | None = Field(default=None, max_length=100)
    dive_center: str | None = Field(default=None, max_length=100)
    certificate_image_url: str | None = None
    memo: str | None = None

    @model_validator(mode="after")
    def check_expiration_after_issue(self) -> "CertificateCreateRequest":
        if self.expiration_date is not None and self.issue_date is not None:
            if self.expiration_date < self.issue_date:
                raise ValueError("expiration_date must be on or after issue_date")
        return self


class CertificateUpdateRequest(BaseModel):
    organization: CertificationOrganization | None = None
    certification_level: str | None = Field(default=None, max_length=100)
    certification_number: str | None = Field(default=None, max_length=100)
    issue_date: date | None = None
    expiration_date: date | None = None
    instructor: str | None = Field(default=None, max_length=100)
    dive_center: str | None = Field(default=None, max_length=100)
    certificate_image_url: str | None = None
    memo: str | None = None


class CertificateResponse(BaseModel):
    id: UUID
    organization: CertificationOrganization
    certification_level: str
    certification_number: str | None
    issue_date: date | None
    expiration_date: date | None
    instructor: str | None
    dive_center: str | None
    certificate_image_url: str | None
    memo: str | None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}
