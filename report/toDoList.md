# API Key 체크리스트 & 배포 체크리스트

## 1. 플랫폼별 API Key / 환경변수 체크리스트

### 외부 서비스 등록 (한 번만 하면 됨)

- [x] **Google Cloud Console** (완료 2026-07-18)
  - [x] OAuth 동의 화면 구성 (앱 이름 "DiveApp"으로)
  - [x] "웹 애플리케이션" 유형 클라이언트 생성 → 승인된 자바스크립트 원본에 `http://localhost:5173` 등록
  - [x] "Android" 유형 클라이언트 생성 → 패키지명 `com.diveapp.android` + 디버그 키스토어 SHA-1 등록
- [x] **Naver Developers** (완료 2026-07-18)
  - [x] 애플리케이션 등록, Client ID/Secret 발급
  - [x] PC 웹 플랫폼 등록 → 콜백 URL `http://localhost:5173/auth/naver/callback`
  - [x] Android 플랫폼 등록 → 패키지명 `com.diveapp.android`
- [ ] **NAVER Cloud Platform (Maps)** — 다이빙 로그 위치 선택 지도용, 위 Naver Developers 로그인 키와는 별개
  - [ ] NCP 콘솔에서 Maps 애플리케이션 생성, Dynamic Map 활성화
  - [ ] Android 패키지명 `com.diveapp.android` 등록
  - [ ] 허용 웹 서비스 URL에 로컬 개발 주소(`http://localhost:5173`)와 배포 도메인(`https://diveapp-web.onrender.com`) 등록
  - [ ] NCP Key ID 발급 → Android는 `Android/local.properties`의 `NAVER_MAP_CLIENT_ID`, Web은 `Web/.env`의 `VITE_NAVER_MAP_NCP_KEY_ID`에 채워넣기 (같은 NCP 애플리케이션에 Android/Web 플랫폼을 함께 등록하면 하나의 Key ID를 공유할 수 있는지는 NCP 콘솔에서 직접 확인 필요)

### Backend/.env

- [x] `DATABASE_URL` — PostgreSQL 연결 문자열
- [x] `JWT_SECRET_KEY` — 임의의 강력한 랜덤 문자열
- [x] `NAVER_CLIENT_ID`
- [x] `NAVER_CLIENT_SECRET`
- [x] `GOOGLE_CLIENT_ID` (Web 클라이언트 ID — Android도 이 값을 공유해서 씀)
- [ ] `CORS_ALLOW_ORIGINS` — 로컬은 기본값으로 충분, 배포 시 실제 도메인으로 변경 필요

### Web/.env (완료 2026-07-18)

- [x] `VITE_GOOGLE_CLIENT_ID` — Backend의 `GOOGLE_CLIENT_ID`와 동일한 값
- [x] `VITE_NAVER_CLIENT_ID`
- [ ] `VITE_NAVER_CALLBACK_URL` — 선택, 비워두면 자동으로 `현재 origin + /auth/naver/callback` 사용 (로컬은 그대로 비워둬도 됨)
- [ ] `VITE_API_BASE_URL` — 선택, 비워두면 `http://localhost:8000`
- [ ] `VITE_NAVER_MAP_NCP_KEY_ID` — NAVER Cloud Platform Maps에서 발급받은 NCP Key ID (다이빙 로그 위치 선택 지도용). 채우기 전까지는 위치 선택 화면 진입 시 인증 실패 에러가 표시됨 (`Docs/13_DiveLogWeb.md`)

### Android/local.properties (아직 비어있음 — 확인 결과 sdk.dir만 있고 아래 값들은 안 채워짐, 2026-07-18)

- [ ] `NAVER_CLIENT_ID`
- [ ] `NAVER_CLIENT_SECRET`
- [ ] `NAVER_CLIENT_NAME` — 네이버 로그인 화면에 표시될 앱 이름 (기본값 "DiveApp")
- [ ] `GOOGLE_WEB_CLIENT_ID` — Web과 동일한 값이어야 함 (2026-07-18 기준 정답: `604875817827-vq6k3qaiu45qdipidsvq3lh1tl5badbg.apps.googleusercontent.com`)
- [ ] `NAVER_MAP_CLIENT_ID` — NAVER Cloud Platform Maps에서 발급받은 NCP Key ID (다이빙 로그 위치 선택 지도용, `NAVER_CLIENT_ID`와는 다른 키). 채우기 전까지는 지도 화면 진입 시 인증 실패 에러가 표시됨 (`Docs/12_DiveLogLocation_NaverMap.md`)

**Web은 재시작 완료, 실제 Naver/Google 로그인 화면까지 도달하는 것 확인함** (2026-07-18) — 단 Google은 Backend/.env와 Web/.env의 `GOOGLE_CLIENT_ID`가 서로 다른 값으로 잘못 채워져 있었던 걸 발견해 Backend 쪽을 Web과 동일하게 수정함. Android는 위 값들을 채우고 Gradle 동기화 후 에뮬레이터/실기기에서 별도 확인 필요.

---

## 2. 배포 체크리스트

### Backend — 완료 (2026-07-18, `Report/11_report_deployment.md`)

- [x] Render에 실제 PostgreSQL 인스턴스 생성 (`basic-256mb` 유료 플랜 — 계정에 이미 무료 DB가 하나 있어서 유료로 진행)
- [x] `alembic upgrade head`를 프로덕션 DB에 대해 실행 — `startCommand`에 포함되어 배포/재시작마다 자동 실행
- [x] `JWT_SECRET_KEY` — Render Blueprint의 `generateValue: true`로 로컬과 다른 값 자동 생성됨
- [x] `ENVIRONMENT=production` 설정
- [x] `CORS_ALLOW_ORIGINS`에 실제 Web 배포 도메인(`https://diveapp-web.onrender.com`) 등록, 실제 preflight 요청으로 확인
- [x] Google Cloud Console에 프로덕션 JS 원본 등록
- [x] Naver Developers에 프로덕션 **서비스 URL과 콜백 URL 둘 다** 등록 (콜백 URL만으로는 부족했음 — "서비스 설정에 오류가 있어" 에러 발생했었음)
- 배포 주소: `https://diveapp-backend.onrender.com`

### Web — 완료 (2026-07-18)

- [x] 정적 호스팅: Render Static Site로 진행 (Backend와 같은 플랫폼으로 통일)
- [x] `VITE_API_BASE_URL`을 프로덕션 백엔드 주소로 설정
- [x] `VITE_GOOGLE_CLIENT_ID`/`VITE_NAVER_CLIENT_ID` 로컬과 동일한 값으로 설정
- [x] 실제 이메일 회원가입 + 실제 Naver/Google 로그인 프로덕션에서 확인 완료
- [ ] 반응형/모바일 브라우저 레이아웃 점검 (현재 데스크톱 해상도로만 확인됨) — 아직 미완료
- 배포 주소: `https://diveapp-web.onrender.com`

### Android

- [ ] Release 서명용 keystore 생성 (사용자가 직접 만들어 안전하게 보관 — 절대 커밋 금지)
- [ ] `ApiConfig.BASE_URL`을 빌드 변형(debug/release)별로 분기해 프로덕션 주소 연결
- [ ] Release 빌드용 Network Security Config 확인 (프로덕션은 HTTPS라 debug처럼 cleartext 허용 설정 불필요)
- [ ] 실물 기기에서 실행 확인 (지금까지는 에뮬레이터에서만 검증)
- [ ] Play Console 등록 및 배포 (스토어에 올릴 경우)

### 공통 / 후순위

- [ ] 실제 브랜드 컬러/타이포그래피 확정 (지금은 임시 색상)
- [ ] 이미지 업로드용 스토리지(S3 등) 연동 — 현재는 URL만 저장 가능
- [x] Android 다이빙 위치 지도 연동 UI 구현 완료 (`Docs/12_DiveLogLocation_NaverMap.md`, 2026-07-22) — 실제 NCP Key ID 발급/입력 및 기기 테스트는 미완료
- [x] Web 다이빙 위치 지도 연동 UI 구현 완료 (`Docs/13_DiveLogWeb.md`, 2026-07-22) — 실제 NCP Key ID 발급/입력 및 브라우저 테스트는 미완료
- [ ] 문서 불일치 항목 결정 — `Requirements.md` vs `DatabaseSchema.md` 필드 차이, 스키마 확장 여부
- [ ] iOS 재개 여부 결정 (현재 보류 중)

세부 배경은 `Report/00_todo_backlog.md`와 각 번호별 리포트 참고.
