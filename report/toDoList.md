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

### Android/local.properties (아직 비어있음 — 확인 결과 sdk.dir만 있고 아래 값들은 안 채워짐, 2026-07-18)

- [ ] `NAVER_CLIENT_ID`
- [ ] `NAVER_CLIENT_SECRET`
- [ ] `NAVER_CLIENT_NAME` — 네이버 로그인 화면에 표시될 앱 이름 (기본값 "DiveApp")
- [ ] `GOOGLE_WEB_CLIENT_ID` — Web과 동일한 값이어야 함 (2026-07-18 기준 정답: `604875817827-vq6k3qaiu45qdipidsvq3lh1tl5badbg.apps.googleusercontent.com`)

**Web은 재시작 완료, 실제 Naver/Google 로그인 화면까지 도달하는 것 확인함** (2026-07-18) — 단 Google은 Backend/.env와 Web/.env의 `GOOGLE_CLIENT_ID`가 서로 다른 값으로 잘못 채워져 있었던 걸 발견해 Backend 쪽을 Web과 동일하게 수정함. Android는 위 값들을 채우고 Gradle 동기화 후 에뮬레이터/실기기에서 별도 확인 필요.

---

## 2. 배포 체크리스트

### Backend

- [ ] Render(또는 다른 호스팅)에 실제 PostgreSQL 인스턴스 생성
- [ ] `alembic upgrade head`를 프로덕션 DB에 대해 실행 (지금까지는 로컬 DB에서만 실행함)
- [ ] `JWT_SECRET_KEY`를 로컬 개발용과 다른 값으로 새로 생성
- [ ] `ENVIRONMENT=production` 설정
- [ ] `CORS_ALLOW_ORIGINS`에 실제 Web 배포 도메인 추가
- [ ] Google/Naver 콘솔에 프로덕션 도메인(콜백 URL / JS origin) 추가 등록

### Web

- [ ] 정적 호스팅 서비스 결정 (Vercel/Netlify/Render Static Site 등)
- [ ] `VITE_API_BASE_URL`을 프로덕션 백엔드 주소로 설정
- [ ] `VITE_GOOGLE_CLIENT_ID`/`VITE_NAVER_CLIENT_ID`는 로컬과 동일한 값 사용 가능 (콘솔에 도메인만 추가하면 됨)
- [ ] `npm run build` 결과물(`dist/`) 배포
- [ ] 반응형/모바일 브라우저 레이아웃 점검 (현재 데스크톱 해상도로만 확인됨)

### Android

- [ ] Release 서명용 keystore 생성 (사용자가 직접 만들어 안전하게 보관 — 절대 커밋 금지)
- [ ] `ApiConfig.BASE_URL`을 빌드 변형(debug/release)별로 분기해 프로덕션 주소 연결
- [ ] Release 빌드용 Network Security Config 확인 (프로덕션은 HTTPS라 debug처럼 cleartext 허용 설정 불필요)
- [ ] 실물 기기에서 실행 확인 (지금까지는 에뮬레이터에서만 검증)
- [ ] Play Console 등록 및 배포 (스토어에 올릴 경우)

### 공통 / 후순위

- [ ] 실제 브랜드 컬러/타이포그래피 확정 (지금은 임시 색상)
- [ ] 이미지 업로드용 스토리지(S3 등) 연동 — 현재는 URL만 저장 가능
- [ ] Naver Map API 키 발급 및 다이빙 위치 지도 연동 (현재 위도/경도 수동 입력)
- [ ] 문서 불일치 항목 결정 — `Requirements.md` vs `DatabaseSchema.md` 필드 차이, 스키마 확장 여부
- [ ] iOS 재개 여부 결정 (현재 보류 중)

세부 배경은 `Report/00_todo_backlog.md`와 각 번호별 리포트 참고.
