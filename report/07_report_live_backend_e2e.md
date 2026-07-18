# Report 07 — Live Backend End-to-End Verification (Android + Web)

## 배경

지금까지 Android(Report 04-05)와 Web(Report 06)은 백엔드 없이 UI 렌더링만 검증했다. 이번에는 **이 환경에 실제로 PostgreSQL과 FastAPI 백엔드를 띄우고, Android와 Web 앱이 실제로 그 백엔드와 통신해 실제 데이터를 주고받는 것까지** 검증했다.

---

## 요약

- 포터블 PostgreSQL 16.4(EnterpriseDB 바이너리 zip, 설치 없이 압축 해제만으로 실행)를 이 환경에 띄우고 로컬 DB(`diveapp`, 포트 5433)를 구성했다.
- Alembic 마이그레이션을 **실제로 처음 실행** — 오프라인 SQL 생성으로는 잡을 수 없었던 **진짜 버그 1건**을 발견하고 수정했다.
- FastAPI 백엔드를 이 DB에 연결해 기동하고, 테스트 계정을 시드해 **Phase 2의 31개 엔드포인트 중 대부분을 실제로 호출**해 검증했다. 이 과정에서 **진짜 버그 1건**(타임존 관련)을 추가로 발견·수정했다.
- Android 앱을 실제 백엔드에 연결해 테스트하는 과정에서 **세 번째 진짜 버그**(Android 9+ cleartext 트래픽 차단)를 발견·수정했다.
- Android와 Web 모두, 실제로 로그인 세션을 재생성해 홈 화면에 실제 닉네임이, 다이브 로그 목록에 실제로 생성한 다이빙 기록이 표시되는 것까지 확인했다 — **이 프로젝트에서 처음으로 완전한 실물 엔드투엔드 검증**이다.

---

## 발견하고 수정한 버그 3건

### 1. Alembic 마이그레이션의 Enum 타입 중복 생성 (Backend)

`0001_initial_schema.py`에서 각 Enum 타입(`auth_provider`, `user_role` 등)을 `op.create_table()` 호출 전에 미리 `enum_type.create(bind, checkfirst=True)`로 생성해두었는데, `op.create_table()`이 컬럼에 사용된 Enum 타입을 **자동으로 다시 생성하려 시도**하면서 `DuplicateObjectError`가 발생했다. 각 Enum이 테이블 하나에서만 쓰이므로 사전 생성 루프 자체가 불필요했다 — 제거해서 해결.

**왜 오프라인 검증(Phase 1)에서 못 잡았나**: `alembic upgrade head --sql`(오프라인 SQL 생성)은 실제 DB에 실행하지 않고 SQL 텍스트만 만들기 때문에, "이미 존재하는 타입을 다시 만들려는" 런타임 오류가 애초에 발생할 수 없었다.

### 2. 여러 모델의 timezone-naive datetime 컬럼 (Backend)

`Banner.start_at/end_at`, `User.last_login_at`, `TrainingRecord.completed_at`, `InformationArticle.published_at`, `AdminLog.created_at` 컬럼이 ORM 모델에서 `mapped_column(nullable=True)`처럼 **명시적 `DateTime(timezone=True)` 없이** 선언되어 있었다. SQLAlchemy는 `datetime` 타입 힌트만으로는 timezone-naive `DateTime()`으로 추론하기 때문에, 쿼리 컴파일 시 timezone-aware 값(`datetime.now(timezone.utc)`)과 타입이 맞지 않아 `GET /api/v1/banners` 호출 시 500 에러가 발생했다.

실제 마이그레이션 파일(`0001_initial_schema.py`)은 처음부터 이 컬럼들을 올바르게 `TIMESTAMPTZ`로 정의해두었었다 — 즉 **DB 스키마는 맞았고 ORM 모델 코드만 스키마와 어긋나 있었다**. 5개 모델 파일에 `DateTime(timezone=True)`를 명시해 수정했고, DB 재생성 없이 바로 해결되었다.

**왜 지금까지 못 잡았나**: 이 불일치는 실제 데이터로 비교 쿼리(`WHERE ... <= now()`)를 실행해야만 드러난다. Phase 1-2의 스모크 테스트는 DB 연결 자체가 안 되는 환경에서 진행되어 이 계층까지 도달하지 못했다.

### 3. Android의 cleartext(HTTP) 트래픽 차단 (Android)

Android 9(API 28)부터는 기본적으로 평문 HTTP 통신을 차단한다. 앱에 Network Security Config가 없어서, 에뮬레이터에서 `http://10.0.2.2:8000`(로컬 백엔드)로 보내는 모든 요청이 `CLEARTEXT communication ... not permitted`로 조용히 실패하고 있었다. 이전 리포트(04-05)에서는 백엔드 자체가 없어 어차피 네트워크 에러가 나는 상황이라 이 문제를 가려서 보지 못했다.

**수정**: `app/src/debug/res/xml/network_security_config.xml`(디버그 빌드 전용, `10.0.2.2`/`localhost`/`127.0.0.1`에 한해 cleartext 허용)과 이를 연결하는 `app/src/debug/AndroidManifest.xml`을 추가했다. **릴리즈 빌드에는 영향 없음** — 프로덕션은 Render의 HTTPS 엔드포인트를 사용하므로 그대로 안전하다.

---

## 실제 엔드투엔드 검증 상세

### Backend API (직접 호출)
테스트 계정을 시드하고 실제 JWT를 발급해 아래를 실제 DB에 대해 호출·검증했다:
- 인증: `GET /users/me`
- Dive Log: 생성(프리다이빙) → 목록 → 통계 → 상세 → 수정
- Certificate: 생성 → 목록 → 삭제
- Training: 생성 → 목록 → 통계
- Community: 게시글 생성 → 목록 → 상세(조회수 증가 확인) → 댓글 생성/조회
- Information/Banner: 공개 목록 조회
- Admin: 대시보드 집계, 회원 목록, 정보글/배너 CRUD (발행 시 `published_at` 자동 설정, 소프트 삭제된 배너의 `display_order` 재사용 가능 확인)
- 검증 실패 케이스: `total_sets=2`(범위 밖) → 422 정상 반환

### Android (에뮬레이터, 실제 백엔드 연결)
네트워크 보안 설정 수정 후, 실제 refresh token으로 부트스트랩 → `POST /auth/refresh` 200 → `GET /users/me` 200을 logcat으로 확인했다. 에뮬레이터가 이 환경의 리소스 제약으로 화면 캡처 직전에 다시 크래시했지만(이전 리포트에서도 관찰된 것과 동일한 환경 이슈, 앱 코드 문제 아님), **인증된 API 호출이 실제로 성공했다는 로그 증거는 확보했다**.

### Web (헤드리스 Chrome, 실제 백엔드 연결)
`puppeteer-core`로 실제 refresh token을 localStorage에 심고 새로고침 → 실제 닉네임("테스트유저님, 안녕하세요!")이 홈 화면에 표시되고, 다이브 로그 목록에 **직접 API로 생성했던 실제 기록**("프리다이빙 · 제주 서귀포 · 2026-07-10 · 최대 18.5m")이 정확히 표시되는 것을 스크린샷으로 확인했다. 콘솔/페이지 에러 없음.

두 플랫폼 모두 테스트 후 임시 토큰 시딩 코드를 원복하고, 원복이 소스(`grep`)와 실제 빌드 양쪽에서 정상임을 재확인했다.

---

## 환경 참고사항

- PostgreSQL: EnterpriseDB 바이너리 zip(`postgresql-16.4-1-windows-x64-binaries.zip`)을 스크래치패드에 다운로드해 압축 해제만으로 실행 (설치 불필요, 포트 5433, `--auth=trust`)
- 이 DB와 서버는 **이 세션/이 컴퓨터에서만 존재하는 임시 개발용**이며 Render 등 실제 배포 환경과 무관하다. 세션 종료 시 사라진다.

---

## 백로그 갱신

- [x] Android/Web 실제 백엔드 엔드투엔드 통합 테스트 — 완료 (본 리포트)
- [x] Alembic 마이그레이션 enum 중복 생성 버그 — 수정 완료
- [x] Banner 등 5개 모델의 timezone-naive datetime 버그 — 수정 완료
- [x] Android cleartext 트래픽 차단 문제 — 디버그 전용 Network Security Config로 수정 완료
- [ ] 위 수정 사항을 iOS 코드(Report 03, 보류 중)에도 동일하게 반영 필요 — iOS 재개 시 확인할 것 (특히 timezone 처리 로직과 로컬 HTTP 연결 시 ATS(App Transport Security) 예외 설정)
- [ ] 실제 Render PostgreSQL 인스턴스에 대한 마이그레이션 실행은 여전히 미수행 (로컬 임시 DB로만 검증)
- [ ] 실제 Naver/Google 계정을 이용한 로그인 자체(OAuth 핸드셰이크)는 여전히 미검증 — 등록된 OAuth 앱과 실사용자 인증이 필요해 이 환경에서 근본적으로 재현 불가

---

## 다음 단계

사용자 지시에 따라 다음 항목(실제 Naver/Google SDK 연동, Admin 대시보드 웹 화면 등)으로 진행한다.
