# Phase 1 Report — Backend Foundation

## 요약

`Docs/09_Architecture.md`, `Docs/11_DatabaseSchema.md`를 기준으로 FastAPI 백엔드 프로젝트의 기반 구조를 구축했다.
Router → Service → Repository → Database 계층 구조를 확립하고, MVP 전체 14개 테이블의 SQLAlchemy 모델과 Alembic 초기 마이그레이션을 작성했으며, 소셜 로그인(Naver/Google) 기반 JWT 인증 흐름을 구현했다.

---

## 구현 범위

### 1. 프로젝트 구조 (`Backend/`)

`09_Architecture.md`의 Backend Project Structure를 그대로 따랐다.

```
Backend/
├── app/
│   ├── core/          # 설정(config), JWT 보안(security), 예외(exceptions)
│   ├── database/      # Base, Mixin, 세션(session)
│   ├── models/         # SQLAlchemy 모델 14종 + Enum
│   ├── schemas/        # Pydantic 요청/응답 모델
│   ├── repositories/   # DB 접근 계층
│   ├── services/       # 비즈니스 로직 계층
│   ├── routers/        # FastAPI 라우터
│   ├── middlewares/     # 전역 예외 핸들러
│   └── main.py
├── alembic/            # 마이그레이션
├── requirements.txt
└── .env.example
```

### 2. 데이터베이스 모델 (14개 테이블)

`11_DatabaseSchema.md`에 정의된 모든 테이블을 컬럼 타입, 제약조건(Check/Unique), 인덱스까지 동일하게 구현했다.

User, UserProfile, DiveLocation, DiveLog, FreedivingLog, ScubaLog, DivePhoto, TrainingRecord, Certificate, CommunityPost, CommunityComment, InformationArticle, Banner, AdminLog

- 테이블명: Singular PascalCase (예: `"DiveLog"`) — 데이터 사전 규칙 준수
- 컬럼명: snake_case
- PK: UUID + `gen_random_uuid()` (PostgreSQL `pgcrypto` extension 자동 생성)
- Soft Delete: `deleted_at` (AdminLog 제외 — 감사 로그는 불변 기록이라 정책상 제외)
- Enum: AccountStatus, AuthProvider, UserRole, DiveType, CertificationOrganization, BannerType

### 3. Alembic 마이그레이션

`alembic/versions/0001_initial_schema.py`에 위 14개 테이블 전체를 수기로 작성했다.
(로컬에 연결 가능한 PostgreSQL이 없어 `--autogenerate` 대신 스키마 문서를 그대로 옮겨 작성 — 검증은 아래 "검증" 참고)

### 4. 인증(Authentication) 도메인

`02_Requirements.md` 3.1 Authentication 기준으로 구현.

- `POST /api/v1/auth/login/{provider}` — Naver/Google 소셜 로그인. 클라이언트가 자체 SDK로 받은 provider token을 서버에 전달하면, 서버가 Naver(`/v1/nid/me`) 또는 Google(`tokeninfo`) API로 검증 후 JWT 발급.
  - 최초 로그인 시 User 레코드만 생성 (`is_new_user: true` 반환) → 클라이언트가 Profile Setup 화면으로 분기
  - 기존 사용자이면서 `DORMANT` 상태면 로그인 시 자동으로 `ACTIVE`로 전환 (요구사항 "로그인 시 휴면 해제" 반영)
  - `SUSPENDED`/`DELETED` 계정은 403으로 거부
- `POST /api/v1/auth/refresh` — Refresh Token으로 Access Token 재발급
- `POST /api/v1/auth/logout` — 인증 필요, 204 반환 (아래 "설계 결정" 참고)
- `POST /api/v1/users/me/profile` — 최초 로그인 후 닉네임 등 프로필 생성 (Profile Setup)
- `GET /api/v1/users/me` — 현재 사용자 + 프로필 조회

### 5. 공통 기반

- JWT 기반 인증 (`python-jose`), Access/Refresh 분리, HTTPBearer
- 전역 예외 처리(`AppException` 계층) → 모든 에러 응답을 `{"error": {"code", "message"}}` 형태로 표준화 (CodingRules.md "Standardize error responses" 반영)
- CORS 미들웨어, `.env` 기반 환경변수 관리

---

## 설계 결정 및 가정 (문서에 명시되지 않아 판단한 부분)

1. **Refresh Token은 Stateless로 처리** — DB 스키마에 Refresh Token 저장 테이블이 정의되어 있지 않아, 서명된 JWT(만료 30일)로만 관리했다. 추후 토큰 폐기(blacklist)가 필요해지면 별도 테이블 추가로 확장 가능한 구조로 설계했다.
2. **`/auth/logout`은 현재 서버 상태를 변경하지 않는 204 응답** — 위 1번과 같은 이유. 클라이언트가 토큰을 폐기하는 것으로 로그아웃을 완료한다. 엔드포인트 자체는 유지해 추후 세션 관리 도입 시 클라이언트 계약을 바꾸지 않아도 되게 했다.
3. **소셜 로그인 흐름은 "클라이언트가 Provider SDK로 토큰을 먼저 획득 → 서버가 검증" 방식으로 가정** — iOS 앱이 네이버/구글 네이티브 SDK로 로그인 후 토큰을 백엔드에 전달하는 일반적인 모바일 OAuth 패턴이다.

## 문서 불일치 발견 (확인 요청)

`06_DataDirectionary.md`의 Enum 설명과 `11_DatabaseSchema.md`의 **User 테이블 컬럼 목록**에 `is_pinned` (Pinned Post) 필드가 포함되어 있는데, 이는 의미상 `CommunityPost`에 속하는 필드로 보인다(실제로 CommunityPost 테이블에도 동일 필드가 정의되어 있음). User 테이블에는 이 필드를 **구현하지 않았다.** 문서의 오탈자로 판단되며, 확인 후 문서 수정이 필요하다.

---

## 검증

- Python 가상환경에서 의존성 설치 후 `app.main` 임포트 및 FastAPI 앱 기동 확인
- `TestClient`로 `/health`, `/api/v1/auth/login/{provider}`, `/api/v1/users/me`, `/api/v1/auth/refresh` 호출 → 인증 실패/토큰 검증 실패 시 표준 에러 포맷으로 정상 응답 확인
- JWT 발급(`create_access_token`/`create_refresh_token`) → 디코딩 왕복 확인, `get_current_token_payload` 의존성이 라우터에 정상 연결됨을 확인 (DB 연결 시도 단계까지 도달 — 실제 PostgreSQL이 없어 연결은 실패하나 이는 예상된 결과)
- `alembic upgrade head --sql` (오프라인 모드)로 `0001_initial_schema.py`가 생성하는 DDL을 검증 — 14개 테이블, 모든 제약조건/인덱스/Enum이 스키마 문서와 일치함을 확인
- 모든 신규 `.py` 파일 `py_compile` 통과

실제 Render PostgreSQL 인스턴스에 대한 `alembic upgrade head` 실행 및 Naver/Google 실제 앱 키를 이용한 OAuth 통합 테스트는 아직 수행하지 못했다 (자격 증명 필요).

---

## 생성/수정 파일

- `Backend/requirements.txt`, `Backend/.env.example`, `Backend/alembic.ini`
- `Backend/alembic/env.py`, `Backend/alembic/script.py.mako`, `Backend/alembic/versions/0001_initial_schema.py`
- `Backend/app/main.py`
- `Backend/app/core/{config,security,exceptions}.py`
- `Backend/app/database/{base,session}.py`
- `Backend/app/models/*.py` (14개 모델 + enums)
- `Backend/app/schemas/{common,auth,user}.py`
- `Backend/app/repositories/user_repository.py`
- `Backend/app/services/{oauth_service,auth_service,user_service}.py`
- `Backend/app/routers/{auth,user}.py`
- `Backend/app/middlewares/exception_handlers.py`
- `.gitignore` (Python/Xcode 관련 항목 추가)

---

## 다음 단계 (Phase 2 예정)

- Dive Log API (Freediving/Scuba), CO₂ Table(Training) API, Certification API, Community API, Information/Banner API, Admin API
- 각 도메인의 Repository/Service/Router 계층 구현 및 이미지 업로드(스토리지 연동) 설계

사용자 지시가 있을 때까지 Phase 2는 진행하지 않는다.
