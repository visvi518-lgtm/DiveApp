# Report 08 — 실제 Naver/Google 로그인 SDK 연동 (Android + Web)

## 배경

사용자가 지시한 순서("1번부터 쭉 진행해보자")의 2번째 항목. Report 07까지는 Naver/Google 로그인이 둘 다 "SDK가 아직 연결되지 않았습니다" 에러를 던지는 스텁이었다. 이번에는 Android와 Web 양쪽 모두 실제 SDK 코드로 교체했다 (iOS는 계속 보류).

사용자가 실제 Naver/Google Client ID/Secret을 이미 발급받았다고 확인해줘서, 코드를 그 값을 읽어 쓰는 형태로 작성했다 — 값 자체는 사용자가 `.env`/`local.properties`에 직접 채워넣기로 함(민감정보라 채팅에 붙여넣지 않음).

---

## 백엔드 계약 확인 (변경 없음)

기존 백엔드(`Backend/app/services/oauth_service.py`)를 먼저 확인한 결과, 클라이언트가 각 제공자의 authorization code를 교환하는 게 아니라 **이미 발급받은 토큰 문자열을 그대로 보내면 백엔드가 검증**하는 구조였다:
- Naver: `POST /auth/login/naver` → body의 `token`을 **Naver access token**으로 간주하고 `openapi.naver.com/v1/nid/me`로 검증
- Google: `POST /auth/login/google` → body의 `token`을 **Google ID token**으로 간주하고 `oauth2.googleapis.com/tokeninfo?id_token=...`으로 검증 (aud를 `GOOGLE_CLIENT_ID`와 비교)

따라서 백엔드 코드/스키마는 전혀 바꾸지 않았고, 클라이언트가 "Naver 액세스 토큰 하나, 구글 ID 토큰 하나"를 정확히 만들어내는 데에만 집중했다.

---

## Web (React + Vite)

`Web/src/core/auth/socialAuth.ts`를 실제 구현으로 교체.

### Google — Identity Services (GSI)
공식 GSI JS는 커스텀 버튼에서 로그인을 트리거하는 공개 API가 없다. 표준 우회법(커뮤니티에서 널리 쓰이는, Google 문서에 명시되지 않은 패턴)을 사용: 화면 밖 숨겨진 컨테이너에 진짜 GSI 버튼을 렌더링해두고, 우리 버튼 클릭 시 숨겨진 버튼을 프로그램적으로 클릭한다. `initialize({ client_id, callback })`의 `callback`이 `response.credential`(ID token, JWT)을 준다.

### Naver — Login JS SDK
`new naver.LoginWithNaverId({ clientId, callbackUrl, isPopup: true, loginButton: {...} })` → `.init()`.

**직접 겪은 버그**: `loginButton` 옵션을 생략하면 SDK가 `#naverIdLogin` 컨테이너에 아무 것도 렌더링하지 않고 조용히 아무 에러도 없이 실패한다. SDK 소스(`naveridlogin_js_sdk_2.0.0.js`)를 직접 받아 디컴파일 없이 문자열 검색으로 확인한 결과, 버튼 생성 로직(`createButtonElement`)이 `this.use` 플래그에 의존하고, 이 플래그는 생성자에 `loginButton` 객체를 넘겼을 때만 `true`가 된다. 커뮤니티 예제 대부분이 이 옵션을 포함하고 있어서 놓치기 쉬운 부분이었다 — headless Chrome + 실제 SDK 스크립트를 로드해 컨테이너의 `innerHTML`을 직접 확인하는 방식으로 재현·확정했다.

팝업 완료 후 access token을 받아오는 흐름:
1. 로그인 버튼 클릭 → Naver 도메인으로 팝업 오픈
2. 사용자가 동의 → 팝업이 등록된 콜백 URL(`/auth/naver/callback`, 새 페이지 `NaverCallbackPage.tsx`)로 리다이렉트
3. 그 페이지가 같은 SDK를 다시 로드해 `getLoginStatus()`로 access token을 읽고 `window.opener.postMessage(...)`로 원래 창에 전달, `window.close()`
4. 원래 창(`socialAuth.ts`)이 `message` 이벤트로 access token을 받아 `signIn()` Promise를 resolve

### 검증
- `npm run build` — 클린 (TS 컴파일 에러 없음)
- headless Chrome + puppeteer-core로: 실제 Client ID 없이 두 버튼 클릭 시 "OO 로그인이 설정되지 않았습니다" 안내 문구가 뜨는 것 확인 (크래시/콘솔 에러 없음)
- 임시로 형식만 유효한 가짜 Client ID를 채워 다시 빌드 → Naver는 실제로 버튼이 렌더링되고 클릭 시 진짜 Naver 도메인으로 팝업이 열리는 것 확인, Google도 GSI 스크립트 로드 후 클릭 시 정상적인 인증 플로우가 시작되는 것 확인 (실제 로그인 완료는 진짜 등록된 앱이 있어야 하므로 여기까지만 검증)
- 테스트 후 임시 `.env`, `puppeteer-core`, 임시 스크립트 모두 삭제

---

## Android (Kotlin + Jetpack Compose)

`Android/app/src/main/java/com/diveapp/android/core/auth/SocialAuthProvider.kt`를 실제 구현으로 교체.

### 의존성 정확도 확보 방법
라이브러리 버전/패키지명/메서드 시그니처를 추측하지 않기 위해, Maven Central(`com.navercorp.nid:oauth`)과 Google Maven(`androidx.credentials`, `com.google.android.libraries.identity.googleid`)에서 실제 AAR/JAR를 내려받아 `javap`으로 바이트코드를 직접 확인한 뒤 코드를 작성했다. 특히 `GoogleIdTokenCredential`에 `TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`과 `TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL` 두 타입 상수가 있다는 것을 발견해, 타입 문자열을 비교하는 대신 `GoogleIdTokenCredential.createFrom(bundle)`을 try-catch로 감싸 파싱 성공 여부로 판정하도록 했다(더 안전).

사용한 버전: `com.navercorp.nid:oauth:5.11.2`, `androidx.credentials:credentials:1.6.0`, `androidx.credentials:credentials-play-services-auth:1.6.0`, `com.google.android.libraries.identity.googleid:googleid:1.2.0` (모두 각 저장소의 최신 안정 버전).

### Naver — NidOAuth
`DiveApplication.onCreate()`에서 `NidOAuth.initialize(this, clientId, clientSecret, clientName)` 호출. 로그인은 `NidOAuth.requestLogin(activity, callback)`을 `suspendCancellableCoroutine`으로 감싸고, 성공 시 `NidOAuth.getAccessToken()`을 반환.

### Google — Credential Manager
`GetSignInWithGoogleOption.Builder(webClientId).setNonce(...).build()` → `CredentialManager.create(activity).getCredential(activity, request)`. **Web 애플리케이션 유형 Client ID**를 Android에서도 `serverClientId`로 사용하는 것이 Google의 공식 아키텍처 — 이렇게 해야 백엔드가 이미 가진 `GOOGLE_CLIENT_ID` 설정 하나로 Web/Android 양쪽 ID 토큰을 모두 검증할 수 있다. 단, Google Cloud Console에는 별도로 "Android" 유형 OAuth 클라이언트(패키지명 + 서명 SHA-1)도 등록되어 있어야 한다.

`Client ID/Secret`은 코드에 하드코딩하지 않고 `local.properties` → `BuildConfig` 필드로 주입 (`local.properties.example` 신규 작성, `local.properties`는 기존에도 gitignore되어 있었음).

### 실기기(에뮬레이터) 검증
1. `./gradlew assembleDebug` — 최초 빌드부터 성공 (의존성 버전 사전 확인 덕분에 재시도 없이 통과)
2. Pixel_9 에뮬레이터에 설치 후 실행 — 크래시 없음. Report 07에서 만든 테스트 계정 세션이 남아있어 바로 홈 화면("테스트유저님, 안녕하세요!")이 뜨는 것으로 기존 백엔드 연동도 여전히 살아있음을 재확인
3. 로그아웃 → 로그인 화면에서 "네이버로 시작하기", "구글로 시작하기" 버튼을 각각 탭
4. `local.properties`에 실제 값이 없는 상태(빈 문자열)에서:
   - 처음에는 (empty-check를 추가하기 전) 두 버튼 모두 SDK 내부로 진입했다 — Google은 `GetSignInWithGoogleOption.Builder`가 `IllegalArgumentException("serverClientId should not be empty")`을 즉시 던져 에러 문구로 이어졌지만, Naver의 `NidOAuth.requestLogin`은 빈 clientId로도 조용히 응답 없는 상태로 오래 멈춰 있었다(크래시는 아님) — UX상 좋지 않아 **양쪽 다 호출 전에 빈 값 여부를 직접 체크해 즉시 친절한 한글 에러를 띄우도록 수정**
   - 수정 후 재빌드·재설치하여 두 버튼 모두 "OO 로그인이 설정되지 않았습니다 (... 누락)" 메시지가 즉시(대기 없이) 뜨는 것을 스크린샷으로 확인 — 크래시 없음, 멈춤 없음

### 미검증 사항
이 환경에는 실제로 등록된 Naver/Google OAuth 앱이 없어서, **진짜 계정으로 끝까지 로그인에 성공하는 흐름 자체는 검증하지 못했다.** 코드가 실제 SDK를 정확한 시그니처로 호출한다는 것과, 설정이 비어있을 때 안전하게 실패한다는 것까지만 확인했다. 사용자가 실제 Client ID/Secret을 등록하고 채워넣은 뒤 직접 최종 확인이 필요하다 (`START.md` 3단계-C).

---

## 문서 갱신

- `DiveApp/START.md`에 "3단계-C: 실제 Naver/Google 로그인 설정" 섹션 추가 — Google Cloud Console / Naver Developers 등록 절차, 디버그 키스토어 SHA-1 추출 명령어, `Web/.env`·`Android/local.properties`·`Backend/.env`에 넣을 정확한 키 이름 정리
- `Web/.env.example`, `Android/local.properties.example` 신규 작성
- `Report/00_todo_backlog.md` 갱신 — Android/Web 섹션의 Naver/Google SDK 항목을 `[x]`로 변경하고 새 "0-4" 섹션에 "실제 계정 로그인 미검증" 항목 기록
- `.claude/Context.md` 갱신 — Current Status/Completed/Current Task 반영 (다음은 3번: Admin 대시보드 웹 화면)

---

## 다음 단계

사용자 지시 순서의 3번째 항목인 **Admin 대시보드 웹 화면** 구현으로 진행한다 (Admin API는 Phase 2에서 이미 완성되어 있고, 대응하는 웹 클라이언트만 없는 상태).

---

## 후속 (2026-07-18): 실제 계정으로 로그인 완주 및 버그 2건 수정

사용자가 실제 Naver/Google OAuth 앱을 등록하고 세 값(Client ID/Secret)을 채운 뒤 Web에서 직접 로그인을 시도했고, 이 과정에서 이 프로젝트에서 처음으로 **진짜 소셜 계정으로 로그인이 끝까지 성공**했다. 그 전에 실제 버그 2건이 나왔다:

1. **Naver 콜백 페이지의 `.init()` 누락** — `NaverCallbackPage`가 SDK 인스턴스를 생성만 하고 `.init()`을 호출하지 않아서, 실제 로그인이 성공해도 SDK가 URL의 OAuth 응답(`access_token`)을 절대 파싱하지 못해 항상 "로그인이 완료되지 않았습니다"로 실패했다. 실제 SDK 소스(`naveridlogin_js_sdk_2.0.0.js`)를 다시 받아 `init()`의 내부 로직(`callbackHandler.isSuccessCallbackRequest()` → `oauthCallback()` → `accessToken` 세팅)을 직접 확인해 원인을 특정했다. `createNaverLoginForCallback()`에 `.init()` 호출과 `#naverIdLogin` 컨테이너 생성을 추가해 수정.
2. **Backend/.env와 Web/.env의 `GOOGLE_CLIENT_ID` 불일치** — 사용자가 실수로 두 파일에 서로 다른 Google OAuth 클라이언트 ID를 채워넣어서, ID 토큰의 `aud` 클레임과 백엔드가 기대하는 `GOOGLE_CLIENT_ID`가 안 맞아 항상 401로 실패했다. 어느 값이 맞는지 사용자에게 확인받고 Backend/.env를 Web과 동일하게 맞춰 해결.

두 버그 모두 수정 후 사용자가 직접 실제 Naver/Google 계정으로 로그인 끝까지 완료하는 것을 확인했다. 반면 Android는 `local.properties`가 여전히 비어있어(이전에 "완료"로 잘못 기록했던 부분, `toDoList.md`에서 정정함) 실제 로그인 테스트는 아직 못했다.
