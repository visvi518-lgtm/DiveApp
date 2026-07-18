# TODO / Task Backlog

각 Phase 리포트에서 발견된 미구현 항목, 미수행 검증, 문서 불일치를 여기에 누적 기록한다.
새 Phase를 진행할 때마다 이 문서를 갱신한다.

상태 표기: `[ ]` 미해결 · `[x]` 해결됨

---

## -1. 배포 (Report 11) — Backend + PostgreSQL + Web, Render, 완료

`https://diveapp-backend.onrender.com`, `https://diveapp-web.onrender.com`. GitHub `github.com/visvi518-lgtm/DiveApp` (Public)에서 `render.yaml` Blueprint로 배포. 실제 Naver/Google 로그인, 이메일 회원가입 프로덕션에서 확인 완료. 상세는 `Report/11_report_deployment.md`.

- [ ] Android는 아직 배포 대상 아님 (Play Store 배포는 keystore 서명 등 별도 준비 필요)
- [ ] 반응형/모바일 브라우저 레이아웃 점검 안 됨
- [ ] Render 무료/저가 플랜은 트래픽 없을 시 슬립 — 실사용 트래픽 늘면 플랜 재검토 필요

---

## 0. 보류 중 — iOS (Phase 3, Mac 환경 필요)

**2026-07-14: 사용자가 iOS 개발을 명시적으로 재개 요청하기 전까지 중단하기로 결정.** Android(완료)와 Web(완료)을 우선 진행한다. 아래 항목은 iOS 재개 시 처리한다.

- [ ] **Mac + Xcode에서 최초 빌드/실행 확인 (최우선)** — 이 세션은 Windows 환경이라 Swift 컴파일러/Xcode가 없어 `Frontend/` 코드를 전혀 컴파일하지 못했다. 괄호 균형 검사와 수동 리뷰로만 검증했다. `xcodegen generate` 후 반드시 빌드 에러를 확인해야 한다.
- [ ] **Report 07에서 발견한 백엔드 버그 2건이 iOS 클라이언트 코드 가정에 영향 없는지 재확인** — (1) Banner 등 일부 timestamp 필드가 이전엔 잘못 직렬화될 수 있었음(백엔드 수정 완료, 클라이언트 영향 없음), (2) iOS도 로컬 HTTP 백엔드로 테스트하려면 ATS(App Transport Security) 예외 설정이 필요할 것 (Android의 cleartext 차단과 동일한 종류의 문제).
- [ ] Naver 로그인 SDK(NidThirdPartyLogin 등) 연동 — 현재 `NaverSocialAuthProvider`는 `sdkNotConfigured` 에러를 던지는 스텁. Android/Web은 Report 08에서 완료했으므로 그 코드 패턴(access token/ID token 구분)을 그대로 이식하면 됨.
- [ ] Google 로그인 SDK(GoogleSignIn-iOS, SPM) 연동 — 현재 `GoogleSocialAuthProvider`는 스텁. Android/Web은 Report 08에서 완료.
- [ ] 실제 브랜드 컬러/타이포그래피 값 정의 후 `Shared/Theme/AppColor.swift`, `AppTypography.swift`에 반영 (현재는 시스템 색상으로 대체)
- [ ] `AppConfig.apiBaseURL`을 빌드 설정(Debug/Release)별로 분기해 Render 배포 주소 연결 (현재 `localhost:8000` 하드코딩)

---

## 0-1. Phase 4 (Android Foundation) 관련 — 실빌드로 검증됨, 남은 항목만 기록

Android는 이 환경에 SDK/에뮬레이터가 있어 **실제 빌드·설치·실행까지 검증 완료**했다 (`Report/04_report_android_foundation.md` 참고). 아래만 남은 항목이다.

- [x] **Naver 로그인 SDK 연동** — 완료 (`Report/08_report_social_login_sdk.md`). `com.navercorp.nid:oauth` + Credential Manager로 실제 access token/ID token을 발급받아 백엔드로 전달. 실제 Client ID/Secret 등록은 `START.md` 3단계-C 참고 (사용자가 직접 값을 채워야 실제 로그인 가능).
- [x] **Google 로그인 SDK 연동** — 완료 (`Report/08_report_social_login_sdk.md`).
- [ ] 실제 브랜드 컬러 확정 후 `Android/app/.../ui/theme/Color.kt` 교체
- [ ] `ApiConfig.BASE_URL`을 빌드 변형별로 분기해 Render 배포 주소 연결 (현재 에뮬레이터 전용 `10.0.2.2` 하드코딩)
- [ ] Release 서명(keystore) 설정 — Debug 빌드만 검증됨, 실기기 배포 전 필요
- [ ] 실물 Android 기기에서의 실행 확인 (에뮬레이터에서만 검증함)

---

## 0-2. Android 기능 화면 (Report 05) 관련 — 화면 렌더링은 실빌드로 검증됨

인증 우회로 UI 렌더링/네비게이션까지는 확인했으나 실제 백엔드 연동 데이터 흐름은 미검증. `Report/05_report_android_feature_screens.md` 참고.

- [x] **실제 백엔드 서버를 띄워 로그인부터 CRUD까지 엔드투엔드 통합 테스트** — 완료 (`Report/07_report_live_backend_e2e.md`). 이 과정에서 Android의 cleartext 트래픽 차단 버그를 발견·수정함.
- [ ] Dive Log 위치 입력 — Naver Map API 연동 없이 위도/경도 수동 입력으로 임시 구현
- [ ] 이미지 업로드(다이브 사진/자격증 이미지/커뮤니티 이미지) 여전히 미구현 — Phase 1부터 이어진 항목
- [ ] Community 검색, Dive Log 필터(종류/기간/지역) UI 미구현 (API는 지원)

---

## 0-3. Web 앱 (Report 06) 관련 — 실브라우저(Chrome headless)로 렌더링·인터랙션 검증됨

`Report/06_report_web_app.md` 참고. CO₂ Table은 puppeteer-core로 실제 클릭·타이머 동작까지 확인.

- [x] **실제 백엔드 서버를 띄운 엔드투엔드 통합 테스트** — 완료 (`Report/07_report_live_backend_e2e.md`). 실제 데이터가 홈/다이브로그 화면에 표시되는 것까지 확인.
- [x] **Naver Login JS SDK / Google Identity Services 실제 연동** — 완료 (`Report/08_report_social_login_sdk.md`). 실제 Client ID 등록은 `START.md` 3단계-C 참고.
- [ ] localStorage 토큰 저장의 XSS 노출 리스크 검토 — 필요시 백엔드에 httpOnly 쿠키 세션 지원 추가 검토
- [ ] Dive Log 위치 입력에 Naver Map API 연동 (위도/경도 수동 입력으로 임시 구현)
- [x] **관리자 대시보드 웹 화면** — 완료 (`Report/09_report_admin_dashboard.md`). 대시보드 통계, 회원 관리(검색/상세/정지·정지해제), 정보 게시판 관리(CRUD), 배너 관리(CRUD) 구현.
- [ ] 반응형/모바일 브라우저 레이아웃 점검 — 데스크톱 해상도로만 확인함

---

## 0-4. 소셜 로그인 SDK 연동 (Report 08) 관련 — Web은 실제 계정으로 완전히 검증 완료

Android(NidOAuth + Credential Manager)와 Web(Naver Login JS SDK + Google Identity Services) 모두 실제 SDK 코드로 교체 완료. `Report/08_report_social_login_sdk.md` 참고.

- [x] **Naver/Google OAuth 앱 등록 완료** (2026-07-18)
- [x] **Web에서 실제 Naver/Google 계정으로 로그인 끝까지 완료 확인** (2026-07-18, 사용자가 직접 테스트) — 그 과정에서 실제 버그 2건을 발견하고 수정함:
  1. **Naver 콜백 페이지가 `.init()`을 호출하지 않던 버그**: `NaverCallbackPage`가 SDK 인스턴스를 만들기만 하고 `.init()`을 안 불러서, 실제 로그인이 성공해도 SDK가 URL의 OAuth 응답(access_token)을 파싱하지 못해 항상 "로그인이 완료되지 않았습니다"로 실패하고 있었음. `com.navercorp...` 실제 SDK 소스를 디컴파일 없이 직접 받아 문자열로 확인해 원인을 특정 — `init()` 내부에서 `callbackHandler.isSuccessCallbackRequest()` 체크 후 `oauthCallback()`이 호출되어야 `accessToken`이 채워지는 구조였음. `createNaverLoginForCallback()`에 `.init()` 호출과 `#naverIdLogin` 컨테이너 생성을 추가해 수정.
  2. **Backend/.env와 Web/.env의 `GOOGLE_CLIENT_ID`가 서로 다른 값으로 채워져 있던 설정 실수**: 사용자가 두 파일에 각각 다른 Google OAuth 클라이언트 ID를 넣어서, 토큰의 `aud` 클레임과 백엔드의 `GOOGLE_CLIENT_ID`가 안 맞아 항상 401(로그인이 만료되었습니다)로 실패하고 있었음. 사용자에게 어느 값이 맞는지 확인받고 Backend/.env를 Web과 동일한 값으로 수정, 재시작 후 해결.
- [ ] iOS에도 동일 기능 이식 (iOS 재개 시)
- [ ] **Android는 아직 `local.properties`에 실제 값이 안 채워져 있음** (`sdk.dir`만 있음, 2026-07-18 확인) — Naver/Google 값 채우고 실기기/에뮬레이터에서 별도 확인 필요
- [ ] Google 로그인 성공 시 Credential Manager가 실기기(에뮬레이터가 아닌)에서도 동일하게 동작하는지 — Google 계정이 없는 에뮬레이터에서는 계정 선택 UI 자체가 뜨지 않을 수 있음 (Android 값 채운 뒤 확인)

---

## 0-5. Admin 대시보드 웹 화면 (Report 09) 관련 — 실제 백엔드+실제 관리자 계정으로 검증됨

`Report/09_report_admin_dashboard.md` 참고. 실제 로컬 백엔드에 테스트 계정을 ADMIN role로 승격시켜 헤드리스 브라우저로 대시보드 통계 조회, 회원 상세 조회, 정보글 작성/삭제, 배너 작성(유니크 제약 위반 에러 메시지 확인 포함)/삭제까지 전부 실제 API 호출로 확인함.

- [ ] Community 게시글/댓글, DiveLog, Training, Certificate에는 Admin API 자체가 없음(백엔드 Phase 2 범위 밖) — 필요시 백엔드 확장부터 선행되어야 함
- [ ] Admin 회원 목록/상세 응답에 `role` 필드가 없어 관리자 화면에서 "이 회원이 이미 관리자인지"를 확인하거나 역할을 변경하는 기능은 만들 수 없었음 — 백엔드 스키마 확장 필요 시 별도 논의
- [ ] 정보글/배너 admin 라우터에는 단건 조회(GET /{id}) 엔드포인트가 없어, 수정 화면은 목록에서 넘겨준 데이터(라우터 state)로 채우고 새로고침 시에만 전체 목록을 다시 불러와 id로 찾는 방식으로 구현함 — 데이터가 많아지면 비효율적이니 백엔드에 단건 조회를 추가하는 게 더 나은 장기 설계
- [ ] AdminLog에 배너/정보글 CRUD가 기록되지 않음 (현재는 회원 정지/정지해제만 기록됨, Backend 기존 제약)
- [ ] 반응형/모바일 레이아웃 미점검 (데스크톱 해상도로만 확인)
- [ ] Admin 화면 자체의 접근 제어는 프론트엔드 role 체크(`RequireAdmin`)에 더해 백엔드 `require_admin` 의존성이 최종 방어선 — 실제 비관리자 계정으로 `/admin/*` API를 직접 호출했을 때 403이 반환되는지는 이번에 재확인하지 않음(Report 02에서 이미 검증된 기존 로직이라 반복하지 않음)

---

## 0-6. 이메일/비밀번호 로그인·회원가입 (Report 10) 관련 — Web만 구현, 실제 백엔드로 전체 플로우 검증됨

Web 로그인 화면에 Naver/Google 소셜 로그인 외에 일반 이메일/비밀번호 로그인·회원가입을 추가했다. 백엔드에 `AuthProvider.EMAIL` 값과 `User.password_hash` 컬럼을 추가하는 마이그레이션이 포함되어 있다. `Report/10_report_email_auth.md` 참고.

- [x] Backend: `POST /auth/register`, `POST /auth/login/email` 추가, bcrypt 해싱, 이메일 형식/비밀번호 길이(8자 이상) 검증 — 완료, curl로 성공/중복가입 409/오답 401/유효성 422 케이스 전부 확인
- [x] Web: LoginPage에 이메일/비밀번호 폼 + 로그인·회원가입 전환 UI 추가 — 완료, 실제 브라우저로 회원가입→프로필설정→홈→로그아웃→재로그인 전체 사이클 확인
- [x] **버그 발견 및 수정**: 회원가입 직후 `/profile-setup`으로 이동은 되지만, 그 화면 자체에 라우트 가드가 없어 프로필 저장 완료 후에도 자동으로 홈으로 이동하지 않던 기존 버그를 발견하고 `RequireProfileSetup` 가드를 추가해 수정함 (소셜 로그인의 신규가입 플로우도 실제 브라우저 클릭으로는 한 번도 끝까지 검증된 적이 없어서 이번에 처음 발견됨)
- [x] **버그 발견 및 수정**: `/login` 페이지가 `needsProfileSetup` 상태일 때 자동으로 `/profile-setup`으로 이동하지 않던 기존 버그도 함께 발견·수정 (`PublicOnly` 가드에 조건 추가)
- [ ] **Android/iOS에는 이메일/비밀번호 로그인 UI가 없음** — 사용자가 "Web 화면"이라고 명시해 이번엔 Web만 구현. 백엔드 API는 플랫폼 중립적이라 나중에 Android/iOS에 이식 가능
- [ ] 이메일 인증(email_verified) 로직 없음 — 가입 시 `email_verified` 컬럼은 그대로 `false`로 남아있고 이를 확인/강제하는 로직이 없음. 실제 이메일 발송 인프라가 필요해 범위 밖으로 둠
- [ ] 비밀번호 재설정(찾기) 기능 없음 — 이것도 이메일 발송 인프라가 필요해 범위 밖
- [ ] 비밀번호 정책은 길이(8자 이상)만 검증 — 복잡도 규칙(특수문자 등)은 의도적으로 넣지 않음(MVP 단순화)
- [ ] Alembic 마이그레이션(`0002_add_email_auth`)의 `ALTER TYPE ... ADD VALUE`는 Postgres 특성상 downgrade로 되돌릴 수 없음(주석으로 명시해둠) — 실제 배포 환경에 적용하기 전에 인지하고 있을 것

---

## 1. 문서 불일치 (Requirements ↔ DatabaseSchema)

`02_Requirements.md`(초기 요구사항)와 `11_DatabaseSchema.md`(실제 DB 스키마) 사이에 여러 필드 누락이 발견되었다.
Phase 2에서는 **`11_DatabaseSchema.md`를 실제 구현의 기준(source of truth)으로 삼고**, 아래 항목은 스키마 확장 여부를 사용자가 결정할 때까지 미구현 상태로 남긴다.

- [ ] **User 테이블의 `is_pinned` 필드** — `CommunityPost`에 속하는 필드로 보이며 User에는 의미가 없음. 문서 오탈자로 추정. (Phase 1에서 발견, 구현 제외)
- [ ] **DiveLog 필드 누락** — `02_Requirements.md` 3.2의 "Common Information"에 명시된 Cover Image, Visibility, Water Temperature, Weather, Dive Buddy, Dive Center, Equipment, Favorite가 `11_DatabaseSchema.md`의 `DiveLog` 테이블에는 없음. 검색 조건 중 "즐겨찾기(Favorite)"도 스키마에 대응 컬럼이 없어 구현 불가.
- [ ] **TrainingRecord에 Memo 필드 없음** — 요구사항 3.3 History에는 "Memo"가 기록 항목으로 명시되어 있으나 스키마에는 없음.
- [ ] **CommunityPost 이미지 첨부 불가** — 요구사항 3.5는 "이미지(최대 10장)"를 게시글 저장 항목으로 명시하지만, 스키마에는 게시글 이미지를 저장할 테이블/컬럼이 없음 (`DivePhoto`와 유사한 `CommunityPostImage` 같은 테이블 부재).
- [ ] **InformationArticle 필드 누락** — 요구사항 3.6은 Category, Summary, Source, Source URL을 저장 항목으로 명시하지만 스키마의 `InformationArticle`에는 title/content/thumbnail_image_url/view_count/is_published/published_at만 존재. 카테고리별 조회 기능이 스키마상 불가능.
- [ ] **Banner의 Description 필드 없음** — 요구사항 3.7은 Description을 배너 저장 항목으로 명시하지만 스키마에는 없음.

→ **결정 필요**: 위 필드들을 추가하는 마이그레이션을 진행할지, 혹은 MVP 범위를 스키마 기준으로 축소 확정할지 사용자 확인 필요.

- [ ] **Banner `display_order` 유니크 제약 없음** — 요구사항 3.7 Validation은 "Display Order 중복 방지"를 명시하지만 `11_DatabaseSchema.md`의 Banner 테이블에는 유니크 제약이 없음. 현재는 서비스 레이어(애플리케이션 코드)에서만 검증하고 있어 동시 요청 시 레이스 컨디션으로 중복이 발생할 수 있음. (Phase 2에서 발견)

---

## 2. Phase 1 (Backend Foundation) 관련 미수행 항목

- [ ] 실제 Render PostgreSQL 인스턴스에 대한 `alembic upgrade head` 실행 (로컬에 접속 가능한 PostgreSQL이 없어 오프라인 SQL 생성으로만 검증함)
- [ ] 실제 Naver/Google OAuth 앱 키를 사용한 로그인 통합 테스트 (현재는 모의(mock) 토큰으로 실패 케이스만 검증)
- [ ] Refresh Token 저장/폐기(blacklist) 전략 확정 — 현재는 stateless JWT로만 구현. 보안 요구사항("Refresh Token 관리")을 더 엄격히 만족하려면 서버 측 세션 테이블 추가를 검토해야 함.

---

## 3. Phase 2 (Domain APIs)에서 의도적으로 제외한 항목

MVP 범위(`02_Requirements.md` 7. MVP Scope)에 따라 아래는 이번 Phase에 구현하지 않는다. (이미 문서의 "제외" 목록과 일치하는 항목은 별도 표시)

- [ ] 실제 이미지 업로드/스토리지 연동 (S3 등) — 현재 API는 클라이언트가 이미 업로드된 이미지의 URL을 전달한다고 가정. 업로드 자체를 처리하는 엔드포인트(presigned URL 발급 등)는 미구현.
- [ ] 커뮤니티 좋아요/북마크/신고/대댓글, 정보글 즐겨찾기/AI 요약, 배너 클릭 통계/A-B 테스트 — 모두 `02_Requirements.md`에 "향후 확장"으로 명시된 항목이므로 계획대로 제외.
- [ ] Admin Role 세분화(Editor/Moderator) — MVP는 Super Admin(단일 ADMIN role)만 지원하기로 문서에 명시되어 있어 그대로 따름.
- [ ] AdminLog의 IP 주소 기록 — 요구사항에 "IP(향후)"로 명시되어 있어 컬럼은 존재하되 현재 요청에서 실제 클라이언트 IP를 채우는 로직은 미구현.

---

## 4. 검증 관련 공통 제약

- [ ] 로컬 환경에 PostgreSQL이 없어 모든 Repository 쿼리는 문법/타입 수준(offline SQL, py_compile, OpenAPI 스키마 생성)으로만 검증되었고, 실제 DB 트랜잭션 동작(제약조건 위반, 트리거 등)은 실제 DB 연결 후 별도 통합 테스트가 필요하다.
- [ ] Phase 2에서 추가된 통계/집계 쿼리(다이빙 통계, CO₂ Table 통계, Admin 대시보드)는 실제 데이터로 결과값 정확성을 검증하지 못했다. 실제 DB 연결 후 샘플 데이터로 재검증 필요.

---

## 5. Phase 2 (Domain APIs) 완료 후 남은 항목

- [x] Dive Log, CO2 Table, Certification, Community, Information, Banner, Admin API 구현 완료 (`Report/02_report_domain_apis.md` 참고)
- [ ] Community 게시글 신고/좋아요/북마크, Information 카테고리, DiveLog 부가 필드 등은 위 "1. 문서 불일치" 섹션의 결정이 내려지기 전까지 계속 보류
- [ ] AdminLog에 실제 클라이언트 IP 기록 (현재 미구현, 3번 항목과 동일)
