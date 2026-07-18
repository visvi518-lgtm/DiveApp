# Report 06 — Web Consumer App (React + Vite)

## 배경

사용자가 iOS 개발을 명시적으로 재개를 요청하기 전까지 중단하고, Android(이미 완료)에 이어 **웹(일반 사용자용, 관리자 대시보드 아님)** 을 우선 진행하기로 결정했다. 웹 기술 스택은 React + Vite로 결정했다.

---

## 요약

Android(Report 04-05)와 동일한 범위 — 인증 흐름(로그인/프로필설정), 5개 도메인(Dive Log, CO₂ Table, Certification, Community, Information) — 를 React + TypeScript + Vite로 구현했다. 백엔드는 Phase 1-2에서 이미 완성된 동일한 REST API를 그대로 사용한다.

**Android/iOS와 마찬가지로 실제로 빌드하고, 브라우저에서 렌더링과 인터랙션(CO₂ Table 타이머 실시간 동작)까지 확인했다.**

---

## 구현 범위 (`Web/`)

### 1. 프로젝트 구성
- `npm create vite@latest -- --template react-ts`로 생성, React 19 + TypeScript + Vite 8
- `react-router-dom`(라우팅), `axios`(HTTP 클라이언트) 추가

### 2. Networking (`core/network`)
- `apiClient.ts` — axios 인스턴스 + 요청/응답 인터셉터. 401 발생 시 Refresh Token으로 자동 갱신 후 1회 재시도, 실패 시 세션 만료 콜백 호출 (Android의 `TokenAuthenticator`와 동일한 설계를 axios 인터셉터로 이식)
- 백엔드의 표준 에러 포맷(`{"error":{"code","message"}}`)을 모든 실패 케이스에서 동일하게 파싱

### 3. Auth 기반 (`core/auth`)
- `tokenStorage.ts` — `localStorage` 기반 (브라우저에는 Keychain/Keystore 대응물이 없고, 백엔드가 Bearer 토큰만 지원해 httpOnly 쿠키 세션은 불가능하므로 현실적 선택)
- `AuthContext.tsx` — React Context로 인증 상태(`bootstrapping`/`unauthenticated`/`needsProfileSetup`/`authenticated`) 관리, iOS의 `AuthSession`/Android의 `AuthSession`과 동일한 상태 모델
- `socialAuth.ts` — Naver/Google 로그인 스텁 (다른 플랫폼과 동일하게 미연동 상태)

### 4. Models/Services (Auth + User + 5개 도메인)
- 백엔드 JSON 필드명을 그대로 사용하는 snake_case TypeScript 인터페이스 (별도 매핑 레이어 없이 단순하게)

### 5. 디자인 시스템
- CSS 커스텀 프로퍼티로 색상/스페이싱 정의 (`theme.css`) — Android `Color.kt`/iOS `AppColor.swift`와 동일한 값
- 공용 컴포넌트: `Button`, `LoadingView`/`EmptyState`/`ErrorState`, `RemoteImage`, `SubPageHeader`, `ListCard`

### 6. 라우팅/화면
- 상단 네비게이션(`AppLayout`) + `react-router-dom` 중첩 라우트로 각 도메인의 목록/상세/작성/수정 화면 구성
- Certification은 `/mypage/certificates/*`, Information은 `/information/*`로 중첩 (모바일의 "탭 안에 중첩 네비게이션" 구조와 동일한 개념을 라우팅으로 구현)
- CO₂ Table은 `setInterval` 기반 실시간 카운트다운 타이머 (Android의 코루틴 타이머와 동일한 로직을 JS로 이식)

---

## 실제 빌드·브라우저 검증 (중요)

- `npm run build`(`tsc -b && vite build`) — **최초 시도부터 클린 빌드 성공** (기반 구조 빌드 시 발견된 TS 에러 3건은 즉시 수정: axios 모듈 확장이 `InternalAxiosRequestConfig`에만 적용되어 공개 API 타입과 불일치했던 문제, TS parameter property가 `erasableSyntaxOnly` 옵션과 충돌하는 문제). 이후 5개 도메인 전체를 추가한 두 번째 빌드는 **에러 없이 한 번에 통과**했다.
- `vite preview`로 프로덕션 빌드를 서빙하고, 이 환경에 설치되어 있던 **Chrome을 헤드리스로 직접 구동**해 스크린샷으로 렌더링을 확인했다.
- 백엔드가 이 세션에 없어, Android 때와 동일하게 `AuthContext`의 초기 상태를 일시적으로 `authenticated`로 하드코딩해 인증 이후 화면을 확인한 뒤 **즉시 원복**했다 (소스에 `TEMP-DEBUG-BYPASS` 문자열이 남아있지 않음을 grep으로 확인, 원복된 빌드가 다시 로그인 화면으로 리다이렉트되는 것을 스크린샷으로 재확인).
  - 확인된 화면: 로그인, 홈(배너/정보게시판 카드), 다이브 로그 목록(에러 상태)과 작성 폼(종류/날짜/위치/종목별 필드), 커뮤니티 목록, 자격증 관리 목록과 추가 폼(드롭다운/날짜 선택), 마이페이지.
  - **CO₂ Table은 한 단계 더 나아가 `puppeteer-core`로 이 환경의 Chrome을 실제로 조작**해 "훈련 시작" 버튼을 클릭하고, 3초 후 타이머가 "2:00"에서 "1:57"로 정확히 카운트다운되는 것을 콘솔 출력으로 확인했다 (콘솔/페이지 에러 없음). 검증 후 `puppeteer-core`는 프로젝트에서 제거했다(개발 의존성으로 남기지 않음).

---

## 설계 결정

1. **관리자 대시보드가 아닌 일반 사용자용 웹앱으로 결정** (사용자 선택) — Admin API(Phase 2에 이미 구현됨)에 대응하는 화면은 여전히 어떤 플랫폼에도 없다 (백로그 참고).
2. **토큰 저장은 `localStorage`** — 브라우저 SPA에는 Keychain/Keystore 같은 보안 저장소가 없고, 백엔드가 Bearer 토큰 방식만 지원하므로 httpOnly 쿠키 세션은 현재 구조상 불가능하다. XSS에 상대적으로 더 노출되는 절충안임을 인지하고 있다 (백로그 참고).
3. **날짜/시간은 별도 라이브러리 없이 문자열로 처리** — `dayjs`/`date-fns` 등을 도입하지 않고 `Date`와 ISO 문자열만으로 표시했다. 표시 포맷이 더 정교해져야 하면 그때 도입을 검토한다.

---

## 백로그 갱신

- [ ] **실제 백엔드 서버를 띄운 엔드투엔드 통합 테스트** (Android와 동일한 항목 — 이제 두 플랫폼 모두 이 항목이 최우선)
- [ ] Naver/Google 실제 로그인 연동 (웹은 Naver Login JS SDK / Google Identity Services 필요)
- [ ] localStorage 토큰 저장의 XSS 노출 리스크 — 필요시 백엔드에 httpOnly 쿠키 기반 세션 지원 추가를 검토
- [ ] Dive Log 위치 입력에 Naver Map API 연동 (Android와 동일하게 위도/경도 수동 입력으로 임시 구현)
- [ ] 이미지 업로드 여전히 미구현 (전 플랫폼 공통 항목)
- [ ] 관리자 대시보드 웹 화면 — Admin API는 이미 있으나 어떤 클라이언트도 아직 구현하지 않음
- [ ] 반응형/모바일 브라우저 레이아웃 점검 — 데스크톱 해상도로만 확인함

---

## 다음 단계

- 사용자 지시가 있을 때까지 다음 단계는 진행하지 않는다.
- iOS는 여전히 Mac 확보가 최우선 (사용자가 재개를 요청할 때까지 보류).
