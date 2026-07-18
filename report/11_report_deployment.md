# Report 11 — Render 배포 (Backend + PostgreSQL + Web)

## 배경

Backend(FastAPI)와 Web(React)을 Render에 실제로 배포했다. `render.yaml` Blueprint로 PostgreSQL + Backend 웹 서비스 + Web 정적 사이트 3개를 한 번에 정의해 진행했다.

또한 이 과정에서 `Android/local.properties.example`(git에 커밋되는 템플릿 파일)에 실제 Naver Client ID/Secret이 잘못 입력되어 있는 걸 발견했다 — 다행히 커밋/푸시 전이라 GitHub에는 노출되지 않았고, 값을 올바른 위치(gitignore된 `local.properties`)로 옮기고 템플릿은 빈 값으로 되돌려 해결했다.

---

## 배포 중 발견하고 수정한 문제 4건

**1. `DATABASE_URL` 드라이버 스킴 불일치**
Render가 제공하는 관리형 PostgreSQL 연결 문자열은 `postgresql://`로 시작하는데, 이 프로젝트는 SQLAlchemy 비동기 엔진(`asyncpg`)을 쓰기 때문에 `postgresql+asyncpg://`가 명시적으로 필요했다. → `app/core/config.py`에 Pydantic `field_validator`를 추가해 `postgresql://`로 시작하면 자동으로 `postgresql+asyncpg://`로 바꿔주도록 처리. 로컬 개발 환경의 기존 URL(이미 `+asyncpg` 포함)은 그대로 통과하므로 로컬 동작에 영향 없음.

**2. 무료 플랜에서 `preDeployCommand` 미지원**
`render.yaml`에 Alembic 마이그레이션을 `preDeployCommand: alembic upgrade head`로 분리해뒀는데, Render 무료 플랜은 이 기능을 지원하지 않아 Blueprint 적용이 실패했다. → `startCommand`에 `alembic upgrade head &&`를 이어붙이는 방식으로 변경 (매 재시작마다 실행되지만 idempotent라 안전).

**3. 계정당 무료 PostgreSQL 1개 제한**
이 Render 계정에 이미 다른 프로젝트용 무료 PostgreSQL이 떠 있어서 `diveapp-db` 생성이 막히고, 연쇄적으로 `diveapp-backend` 생성도 취소됐다. → 기존 무료 DB는 그대로 두고, DiveApp용 DB만 유료 플랜(`basic-256mb`)으로 변경해 해결.

**4. Naver "서비스 설정에 오류가 있어" 에러 — 콜백 URL만으로는 부족**
Naver Developers에 콜백 URL(`.../auth/naver/callback`)만 추가하고 실제 로그인을 시도하니 "DiveApp 서비스 설정에 오류가 있어 로그인할 수 없습니다" 에러가 발생했다. 원인은 콜백 URL과 별개로 **서비스 URL(도메인 자체)** 도 함께 등록해야 하는데 그 부분이 로컬 주소로만 남아있었기 때문. → Naver Developers의 "서비스 URL" 필드에도 프로덕션 도메인을 추가해 해결.

---

## 실제 검증

- `GET /health` → `200 {"status":"ok"}`
- CORS preflight (`OPTIONS` + `Origin: https://diveapp-web.onrender.com`) → `access-control-allow-origin`이 정확히 반영됨을 확인
- 배포된 Web 번들에서 `VITE_API_BASE_URL`/`VITE_GOOGLE_CLIENT_ID`/`VITE_NAVER_CLIENT_ID`가 올바른 값으로 빌드에 박혀있는 것을 직접 확인 (문자열 검색)
- `POST /api/v1/auth/register`를 프로덕션 주소로 직접 호출해 실제 프로덕션 DB에 계정 생성 + JWT 발급까지 확인 (Alembic 마이그레이션이 실제로 적용되었다는 뜻)
- 사용자가 직접 `https://diveapp-web.onrender.com`에서 **실제 Naver 계정, 실제 Google 계정으로 로그인 완료** 확인

---

## 현재 배포 상태

| | URL |
|---|---|
| Backend | `https://diveapp-backend.onrender.com` |
| Web | `https://diveapp-web.onrender.com` |
| DB | Render PostgreSQL (`basic-256mb` 유료 플랜) |

GitHub 저장소: `github.com/visvi518-lgtm/DiveApp` (Public, `.env`/`local.properties` 등 시크릿은 전부 gitignore 처리됨)

## 남은 것

- Android는 아직 배포 대상이 아님 (스토어 배포는 keystore 서명 등 별도 준비 필요, `toDoList.md` 참고)
- Render 무료 정적 사이트/웹 서비스는 트래픽 없을 시 슬립 상태로 들어갈 수 있음 — 첫 요청이 느릴 수 있다는 점 참고
- iOS는 계속 보류 중
