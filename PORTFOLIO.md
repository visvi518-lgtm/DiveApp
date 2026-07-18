# DiveApp — 프리다이버·스쿠버다이버를 위한 크로스플랫폼 다이빙 로그 플랫폼

개인 풀스택 프로젝트 · Backend(FastAPI) · Android(Kotlin/Compose) · Web(React/TypeScript)

---

## 한 줄 요약

다이빙 기록, CO₂ Table 훈련, 자격증 관리, 커뮤니티를 하나로 묶은 다이빙 플랫폼을 기획 문서 작성부터 DB 스키마 설계, 3개 플랫폼(백엔드·Android·Web) 구현, 실제 기기/브라우저/백엔드를 연동한 End-to-End 검증까지 전 과정을 진행했다. AI 페어 프로그래밍 도구(Claude Code)를 활용해 스캐폴딩 속도를 확보하고, 그 시간을 실제 환경에서의 검증과 디버깅에 집중적으로 투자.

## 프로젝트 개요

프리다이버, 스쿠버다이버, 다이빙 강사 및 교육기관을 대상으로, 다이빙 기록·CO₂ Table 훈련·자격증 관리·커뮤니티·정보 게시판·관리자 대시보드 기능을 제공하는 서비스로 기획. 실제 서비스 출시를 전제로 요구사항 정의(`02_Requirements.md`) → 유저플로우(`03_UserFlow.md`) → DB 스키마(`11_DatabaseSchema.md`) → API 설계 → 화면 설계 순서로 문서를 먼저 작성한 뒤 구현에 들어가는 Documentation-First 워크플로우 진행.

**원래 계획은 iOS(SwiftUI) 우선 개발.** iOS 파운데이션 구현까지 마친 시점에 개발 환경에 Mac/Xcode가 없어 실제 컴파일 검증이 불가능하다는 제약이 드러났고, "검증 가능한 것부터 완성도를 높인다"는 원칙에 따라 iOS는 보류하고 Android(Kotlin/Compose)와 Web(React)을 우선 진행하는 방향으로 전환. 이 판단 덕분에 이후 모든 기능을 실제 에뮬레이터·브라우저·로컬 백엔드로 끝까지 검증.

### 기능 범위

| 기능            | 설명                                                       |
| --------------- | ---------------------------------------------------------- |
| 로그인          | Naver/Google 소셜 로그인 + 이메일/비밀번호 로그인·회원가입 |
| 다이브 로그     | 프리다이빙/스쿠버 다이빙 기록, 통계(총 횟수·최대 수심)     |
| CO₂ Table       | 프리다이빙 훈련용 세트 타이머                              |
| 자격증 관리     | 다이빙 자격증(AIDA/PADI/SSI 등) 등록·관리                  |
| 커뮤니티        | 게시글·댓글                                                |
| 정보 게시판     | 다이빙 관련 소식/정보                                      |
| 관리자 대시보드 | 회원 관리, 정보글/배너 CRUD, 통계                          |

---

## 아키텍처

```
Backend  : Router → Service → Repository → Database
Frontend : View → ViewModel → Repository → Service → API Client → Backend API
```

- **Backend**: FastAPI + SQLAlchemy 2.0(async) + PostgreSQL + Alembic 마이그레이션, JWT 인증(python-jose), 계층형 아키텍처(Router/Service/Repository 분리)
- **MVP 스키마**: 14개 테이블(User, UserProfile, DiveLocation, DiveLog, FreedivingLog, ScubaLog, DivePhoto, TrainingRecord, Certificate, CommunityPost, CommunityComment, InformationArticle, Banner, AdminLog)
- **API**: 총 33개 엔드포인트 (Auth/User/DiveLog/Training/Certificate/Community/Information/Banner/Admin, 이메일 인증 2건 추가 포함)

### 플랫폼별 스택

|                 | Backend                                         | Android                                  | Web                                          |
| --------------- | ----------------------------------------------- | ---------------------------------------- | -------------------------------------------- |
| 언어/프레임워크 | Python, FastAPI                                 | Kotlin, Jetpack Compose                  | React 19, TypeScript, Vite                   |
| 데이터          | SQLAlchemy 2.0(async), Alembic, PostgreSQL 16.4 | Retrofit + OkHttp, kotlinx.serialization | axios, react-router-dom                      |
| 인증 저장       | JWT(access/refresh)                             | EncryptedSharedPreferences               | localStorage                                 |
| 소셜 로그인     | Naver/Google 토큰 검증                          | NidOAuth, Credential Manager             | Naver Login JS SDK, Google Identity Services |

---

## 문제 해결 사례

가장 많은 것을 배운 부분은 기능 구현 자체보다 **"실제로 돌려봐야만 드러나는 버그"** 를 찾아 고친 과정. 오프라인 스모크 테스트, 목(mock) 기반 테스트만으로는 절대 잡을 수 없었던 문제.

### 세션 A — 로컬 PostgreSQL + 실제 백엔드 연동 (총 3건)

**A1. Alembic 마이그레이션의 Enum 타입 중복 생성**
DB 마이그레이션 파일에서 Enum 타입을 `op.create_table()` 실행 전에 미리 생성해두었는데, `create_table()`이 컬럼에 쓰인 Enum 타입을 자동으로 다시 생성하려고 시도하면서 `DuplicateObjectError`가 발생. 오프라인 SQL 생성(`--sql` 옵션)으로는 실제 DB에 실행하지 않기 때문에 이 문제가 드러나지 않음. → 불필요한 사전 생성 로직 제거로 해결.

**A2. Timezone-naive datetime 컬럼**
배너/유저 등 5개 모델의 날짜 컬럼이 `DateTime(timezone=True)` 명시 없이 선언되어 있어, 실제 timezone-aware 값과 비교하는 쿼리에서 500 에러가 발생. 마이그레이션 DDL 자체는 처음부터 `TIMESTAMPTZ`로 올바르게 정의되어 있었고, ORM 모델 코드만 스키마와 어긋나 있었음. → 5개 모델 파일에 `timezone=True` 추가로 해결, DB 재생성 불필요.

**A3. Android 9+ 평문 HTTP 통신 차단**
Network Security Config가 없어 에뮬레이터에서 로컬 백엔드로 보내는 모든 요청이 `CLEARTEXT communication not permitted`로 조용히 실패. → 디버그 빌드 전용 Network Security Config를 추가해 해결 (릴리즈 빌드는 영향 없음, HTTPS 사용).

### 세션 B — 실제 소셜 계정으로 로그인 완주 (총 2건)

**B1. Naver 로그인 콜백 페이지의 `.init()` 누락**
콜백 페이지에서 SDK 인스턴스를 생성만 하고 `.init()`을 호출하지 않아서, 실제 로그인이 성공해도 SDK가 URL의 OAuth 응답을 절대 파싱하지 못해 항상 "로그인이 완료되지 않았습니다"로 실패. 실제 SDK 소스 코드를 직접 읽어 `init()` 내부에서 `callbackHandler.isSuccessCallbackRequest()` → `oauthCallback()` → `accessToken` 세팅으로 이어지는 흐름을 확인해 원인을 특정. → `.init()` 호출과 필수 DOM 컨테이너 추가로 해결.

**B2. 두 개의 서로 다른 Google OAuth Client ID**
Backend와 Web 각각의 환경변수 파일에 실수로 서로 다른 Google OAuth 클라이언트 ID가 채워져 있어서, ID 토큰의 `aud` 클레임이 백엔드가 기대하는 값과 맞지 않아 항상 401로 실패. → 두 값을 하나로 통일해 해결.

두 버그를 모두 고친 뒤, 실제 본인 Naver·Google 계정으로 로그인을 끝까지 완주해 확인.

### 세션 C — 이메일/비밀번호 인증 추가 중 발견 (총 2건, 소셜 로그인에도 있던 기존 버그)

이메일 회원가입 → 프로필 설정 → 홈 → 로그아웃 → 재로그인까지 처음으로 실제 브라우저에서 전체 사이클을 클릭해보다가 발견.

**C1. 프로필 설정 화면에 라우트 가드 없음** — 저장 API가 성공해도 다른 화면으로 이동시키는 로직 자체가 없어 계속 그 화면에 머물러 있었음.
**C2. 로그인 화면이 "프로필 설정 필요" 상태를 처리하지 않음** — 신규가입 직후에도 로그인 화면에 그대로 남아있음.

두 버그 모두 원래 Naver/Google 신규가입 플로우에도 존재했지만, 지금까지 그 경로를 실제로 끝까지 클릭해본 적이 없어 발견되지 못했던 것들이라서 라우트 가드 2개를 추가해 해결.

---

## 회고

일곱 건의 버그 모두 공통점 — **오프라인/목 기반 테스트로는 원천적으로 발견할 수 없고, 실제 DB·실제 기기·실제 계정으로 전체 플로우를 끝까지 돌려봐야만 드러나는 문제**였다는 점. 기능을 "구현했다"는 것과 "실제로 동작한다"는 것 사이의 간극을 좁히는 데 개발 과정의 상당 부분을 썼고, 이 프로젝트를 통해 로컬 개발 환경에서도 가능한 한 실제 인프라(진짜 DB, 진짜 OAuth 앱, 진짜 기기)를 갖추고 검증하는 습관의 가치를 다시 확인.
