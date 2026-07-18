from datetime import datetime, timedelta, timezone
from uuid import UUID

import bcrypt
from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt

from app.core.config import settings
from app.core.exceptions import AppException

security_scheme = HTTPBearer()

ACCESS_TOKEN_TYPE = "access"
REFRESH_TOKEN_TYPE = "refresh"


def _create_token(subject: str, token_type: str, expires_delta: timedelta, extra_claims: dict | None = None) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": subject,
        "type": token_type,
        "iat": now,
        "exp": now + expires_delta,
    }
    if extra_claims:
        payload.update(extra_claims)
    return jwt.encode(payload, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)


def create_access_token(user_id: UUID, role: str) -> str:
    return _create_token(
        subject=str(user_id),
        token_type=ACCESS_TOKEN_TYPE,
        expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
        extra_claims={"role": role},
    )


def create_refresh_token(user_id: UUID) -> str:
    return _create_token(
        subject=str(user_id),
        token_type=REFRESH_TOKEN_TYPE,
        expires_delta=timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
    )


def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, settings.JWT_SECRET_KEY, algorithms=[settings.JWT_ALGORITHM])
    except JWTError as exc:
        raise AppException(status_code=401, code="INVALID_TOKEN", message="Invalid or expired token") from exc


class TokenPayload:
    def __init__(self, user_id: UUID, role: str):
        self.user_id = user_id
        self.role = role


def get_current_token_payload(
    credentials: HTTPAuthorizationCredentials = Depends(security_scheme),
) -> TokenPayload:
    payload = decode_token(credentials.credentials)
    if payload.get("type") != ACCESS_TOKEN_TYPE:
        raise AppException(status_code=401, code="INVALID_TOKEN", message="Access token required")
    return TokenPayload(user_id=UUID(payload["sub"]), role=payload.get("role", "USER"))


def require_admin(token: TokenPayload = Depends(get_current_token_payload)) -> TokenPayload:
    if token.role != "ADMIN":
        raise AppException(status_code=403, code="FORBIDDEN", message="Admin role required")
    return token


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(password: str, password_hash: str) -> bool:
    return bcrypt.checkpw(password.encode("utf-8"), password_hash.encode("utf-8"))
