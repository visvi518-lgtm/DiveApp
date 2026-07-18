# DiveApp

프리다이빙과 스쿠버다이빙 사용자를 위한 다이빙 기록, CO2 Table 훈련, 자격증 관리, 커뮤니티, 운영자 관리 기능을 제공하는 멀티 플랫폼 서비스입니다.

이 프로젝트는 신입 개발자 포트폴리오 용도로, 단순 화면 구현보다 **요구사항 분석 → DB/API 설계 → 백엔드 구현 → Web/Android 클라이언트 연동 → 실제 엔드투엔드 검증** 흐름을 보여주는 데 초점을 두었습니다.

---

## 프로젝트 요약

### 핵심 기능

- 이메일/비밀번호, Naver, Google 기반 인증 구조
- JWT Access Token / Refresh Token 인증 흐름
- 다이브 로그 CRUD 및 통계
- CO2 Table 훈련 기록 및 통계
- 자격증 등록/조회/수정/삭제
- 커뮤니티 게시글/댓글 기능
- 정보글, 배너, 회원 관리를 포함한 관리자 대시보드
- Web, Android 클라이언트와 REST API 연동

### 구현 상태

| 영역 | 상태 | 설명 |
| --- | --- | --- |
| Backend | 구현 및 로컬 E2E 검증 완료 | FastAPI, SQLAlchemy, Alembic, PostgreSQL |
| Web | 구현 및 브라우저 검증 완료 | React, TypeScript, Vite |
| Android | 주요 화면 및 API 연동 검증 | Kotlin, Jetpack Compose, Retrofit |
| Admin | Web 관리자 화면 구현 | 대시보드, 회원, 정보글, 배너 관리 |

---

## 기술 스택

### Backend

- Python 3.11+
- FastAPI
- SQLAlchemy 2.x
- Alembic
- PostgreSQL
- Pydantic
- JWT 인증

### Web

- React
- TypeScript
- Vite
- React Router
- Axios
- Oxlint

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Retrofit / OkHttp
- Kotlinx Serialization
- AndroidX Security Crypto
- Naver Login SDK
- Google Credential Manager

## 아키텍처

### Backend

```text
Router
  -> Service
    -> Repository
      -> SQLAlchemy Model
        -> PostgreSQL
```

- Router는 요청/응답과 의존성 주입만 담당합니다.
- Service는 인증, 권한, 검증, 비즈니스 규칙을 담당합니다.
- Repository는 DB 접근을 캡슐화합니다.
- Schema와 Model을 분리해 API 계약과 영속성 모델의 책임을 나눴습니다.

### Client

```text
View
  -> ViewModel / Context
    -> Repository
      -> Service
        -> API Client
```

- 화면 상태와 네트워크 로직을 분리했습니다.
- Web과 Android가 동일한 REST API 계약을 바라보도록 설계했습니다.
- 토큰 저장소, API 에러, 인증 세션을 공통 관심사로 분리했습니다.

---

## 주요 구현 경험

### 1. 실제 서비스 흐름을 고려한 인증

- 소셜 로그인과 이메일 로그인을 같은 `TokenResponse` 계약으로 통합했습니다.
- 신규 가입자는 `is_new_user` 플래그로 프로필 설정 화면으로 이동합니다.
- Access Token 재발급과 Refresh Token 저장 흐름을 Web/Android에서 검증했습니다.
- 로그인 실패 시 계정 존재 여부가 노출되지 않도록 동일한 오류 메시지를 반환합니다.

### 2. 도메인 API 설계와 구현

- 다이브 로그, 훈련, 자격증, 커뮤니티, 정보글, 배너, 관리자 기능을 REST API로 분리했습니다.
- 사용자 권한과 관리자 권한을 구분했습니다.
- 관리자 작업 일부는 Audit Log로 기록할 수 있도록 확장 지점을 마련했습니다.

### 3. 실제 DB 기반 E2E 검증

로컬 PostgreSQL과 FastAPI 서버를 실제로 띄운 뒤 다음 흐름을 검증했습니다.

- Alembic 마이그레이션 실행
- JWT 발급 및 인증 요청
- 다이브 로그 생성/목록/상세/수정
- CO2 훈련 기록 및 통계 조회
- 자격증 생성/삭제
- 커뮤니티 게시글/댓글 작성
- 관리자 대시보드 통계 조회
- Web 화면에서 실제 DB 데이터 렌더링
- Android 앱에서 실제 백엔드 인증 API 호출

검증 과정에서 Alembic enum 중복 생성, timezone-aware datetime 불일치, Android debug HTTP 통신 차단 문제를 발견하고 수정했습니다.

---

## 폴더 구조

```text
DiveApp
├─ Backend/                  # FastAPI 백엔드
│  ├─ app/
│  │  ├─ routers/            # API 라우터
│  │  ├─ services/           # 비즈니스 로직
│  │  ├─ repositories/       # DB 접근
│  │  ├─ models/             # SQLAlchemy 모델
│  │  ├─ schemas/            # Pydantic 스키마
│  │  ├─ core/               # 설정, 보안, 예외
│  │  └─ database/           # DB 세션
│  └─ alembic/               # DB 마이그레이션
├─ Web/                      # React Web 앱
│  └─ src/
│     ├─ pages/              # 사용자/관리자 화면
│     ├─ services/           # API 서비스
│     ├─ models/             # API 타입
│     ├─ core/               # 인증/네트워크
│     └─ components/         # 공통 UI
├─ Android/                  # Kotlin Android 앱
├─ Docs/                     # 요구사항, ERD, 아키텍처 문서
└─ report/                   # 단계별 개발/검증 리포트
```

---

## 실행 방법

자세한 로컬 실행 가이드는 [START.md](./START.md)를 참고하세요.

### Backend

```bash
cd Backend
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
alembic upgrade head
uvicorn app.main:app --reload --port 8000
```

### Web

```bash
cd Web
npm install
npm run dev
```

기본 접속 주소는 `http://localhost:5173`입니다.

### Android

Android Studio에서 `Android/` 폴더를 열고 Gradle 동기화 후 실행합니다.

---

## 포트폴리오에서 강조할 포인트

- 요구사항 문서, ERD, API, 구현, 검증 리포트를 함께 관리했습니다.
- 백엔드와 클라이언트를 따로 만든 것이 아니라 실제 API 계약으로 연결했습니다.
- 단순 성공 케이스뿐 아니라 마이그레이션, 시간대, Android 네트워크 정책 같은 실전형 문제를 발견하고 해결했습니다.
- Web 관리자 화면까지 구현해 사용자 앱과 운영 도구의 관점을 함께 다뤘습니다.
- 남은 과제와 기술 부채를 숨기지 않고 backlog로 관리했습니다.

---

## 남은 과제

- 실제 Render PostgreSQL 배포 환경 마이그레이션 검증
- 실제 Naver/Google OAuth 앱 키를 사용한 계정 로그인 검증
- 이미지 업로드 스토리지 연동
- Naver Map 기반 위치 선택
- Web/Android 반응형 및 실기기 추가 검증
- Refresh Token 서버 저장/폐기 전략 고도화

---

## 문서

- [요구사항](./Docs/02_Requirements.md)
- [기능 맵](./Docs/04_FeatureMap.md)
- [아키텍처](./Docs/09_Architecture.md)
- [ERD](./Docs/10_ERD.md)
- [DB 스키마](./Docs/11_DatabaseSchema.md)
- [개발 백로그](./report/00_todo_backlog.md)
- [Live Backend E2E 검증 리포트](./report/07_report_live_backend_e2e.md)
- [이메일 인증 구현 리포트](./report/10_report_email_auth.md)

---

## 한 줄 소개

**DiveApp은 다이빙 도메인의 사용자 앱과 운영자 도구를 하나의 REST API 기반으로 연결한 풀스택 포트폴리오 프로젝트입니다.**
