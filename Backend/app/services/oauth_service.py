import httpx

from app.core.config import settings
from app.core.exceptions import UnauthorizedException
from app.models.enums import AuthProvider

NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me"
GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo"


class OAuthUserInfo:
    def __init__(self, provider_user_id: str, email: str, email_verified: bool):
        self.provider_user_id = provider_user_id
        self.email = email
        self.email_verified = email_verified


async def _verify_naver_token(access_token: str) -> OAuthUserInfo:
    async with httpx.AsyncClient(timeout=5.0) as client:
        response = await client.get(
            NAVER_PROFILE_URL, headers={"Authorization": f"Bearer {access_token}"}
        )
    if response.status_code != 200:
        raise UnauthorizedException("Failed to verify Naver token")

    body = response.json()
    if body.get("resultcode") != "00":
        raise UnauthorizedException("Invalid Naver token")

    profile = body.get("response", {})
    provider_user_id = profile.get("id")
    email = profile.get("email")
    if not provider_user_id or not email:
        raise UnauthorizedException("Naver profile is missing required fields")

    return OAuthUserInfo(provider_user_id=provider_user_id, email=email, email_verified=True)


async def _verify_google_token(id_token: str) -> OAuthUserInfo:
    async with httpx.AsyncClient(timeout=5.0) as client:
        response = await client.get(GOOGLE_TOKENINFO_URL, params={"id_token": id_token})
    if response.status_code != 200:
        raise UnauthorizedException("Failed to verify Google token")

    body = response.json()
    if settings.GOOGLE_CLIENT_ID and body.get("aud") != settings.GOOGLE_CLIENT_ID:
        raise UnauthorizedException("Google token audience mismatch")

    provider_user_id = body.get("sub")
    email = body.get("email")
    if not provider_user_id or not email:
        raise UnauthorizedException("Google profile is missing required fields")

    return OAuthUserInfo(
        provider_user_id=provider_user_id,
        email=email,
        email_verified=body.get("email_verified") in ("true", True),
    )


async def verify_oauth_token(provider: AuthProvider, token: str) -> OAuthUserInfo:
    if provider == AuthProvider.NAVER:
        return await _verify_naver_token(token)
    if provider == AuthProvider.GOOGLE:
        return await _verify_google_token(token)
    raise UnauthorizedException(f"Unsupported provider: {provider}")
