from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.middlewares.exception_handlers import register_exception_handlers
from app.routers import admin, auth, banner, certificate, community, dive_log, information, training, user

app = FastAPI(title="DiveApp API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

register_exception_handlers(app)

app.include_router(auth.router)
app.include_router(user.router)
app.include_router(dive_log.router)
app.include_router(training.router)
app.include_router(certificate.router)
app.include_router(community.router)
app.include_router(information.router)
app.include_router(information.admin_router)
app.include_router(banner.router)
app.include_router(banner.admin_router)
app.include_router(admin.router)


@app.get("/health", tags=["health"])
async def health_check() -> dict:
    return {"status": "ok"}
