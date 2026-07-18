# Phase 4 Report — Android Frontend Foundation (조기 착수)

## 배경

Phase 3(iOS Foundation)는 이 작업 환경에 Xcode가 없어 컴파일 검증을 전혀 하지 못한 채로 마무리되었다. 사용자가 macOS를 준비하기 전까지, `Docs/02_Requirements.md`가 명시한 "향후 안드로이드/웹 확장" 목표를 앞당겨 **Android 네이티브 앱(Kotlin + Jetpack Compose)의 기반을 구축**하고, **이번에는 실제로 빌드·설치·실행까지 전부 검증**했다.

---

## 요약

iOS Phase 3와 동일한 범위(네트워킹, 인증 흐름, 디자인 시스템, 탭 네비게이션)를 Android로 이식했다. 백엔드는 Phase 1~2에서 이미 플랫폼 중립적인 REST API로 구축되어 있어 **백엔드 변경 없이** 새 클라이언트만 추가하면 되는 구조였음을 그대로 확인했다.

**이번 Phase는 iOS와 달리 실제 컴파일, APK 생성, 에뮬레이터 설치·실행, 화면 렌더링, 버튼 탭 동작까지 전부 실기기(에뮬레이터)에서 확인했다.**

---

## 이 환경에서 실제로 가능했던 것 (중요)

이 작업 환경(Windows)에는 미리 설치된 **Android Studio, Android SDK(플랫폼 36, 빌드도구 36.1.0/37.0.0), 에뮬레이터(Pixel_9, Pixel_10 AVD)** 와 JDK가 이미 존재했고 인터넷 접속도 가능했다. iOS와 달리 Android는 Windows에서도 정식으로 개발 가능한 플랫폼이기 때문에 실제 빌드 파이프라인을 그대로 사용할 수 있었다.

- Gradle 8.9를 직접 내려받아 프로젝트를 빌드하고, 이후 프로젝트 자체의 `./gradlew`(wrapper)를 생성해 앞으로는 이 도구 설치 없이도 빌드 가능하도록 만들었다.
- AGP(Android Gradle Plugin)가 필요한 SDK Build-Tools 34, Platform 36을 라이선스 수락 후 자동으로 내려받아 설치했다.
- `assembleDebug`로 실제 디버그 APK(`app-debug.apk`, 약 18.7MB) 생성 성공.
- Pixel_9 에뮬레이터를 부팅해 APK를 설치하고 앱을 실행 — **크래시 없이 로그인 화면이 정상 렌더링**되는 것을 스크린샷으로 확인했다.
- "네이버로 시작하기" 버튼을 탭해 실제 터치 이벤트가 ViewModel까지 전달되고, 스텁 소셜 로그인 프로바이더가 예외를 던지고, 그 에러 메시지("네이버 로그인 SDK가 아직 연결되지 않았습니다.")가 화면에 정확히 표시되는 것까지 확인했다.
- `logcat`에서 크래시(FATAL, AndroidRuntime 예외) 없음을 확인했다.

즉 이번 Phase는 "코드를 작성했다"가 아니라 **"실제로 동작하는 것을 눈으로 확인했다"**는 점에서 iOS Phase 3와 검증 수준이 다르다.

---

## 구현 범위 (`Android/`)

### 1. 프로젝트 구성
- Gradle Kotlin DSL, AGP 8.7.2, Kotlin 2.0.20, compileSdk/targetSdk 36, minSdk 26
- 패키지: `com.diveapp.android`
- 어댑티브 런처 아이콘(벡터, 바이너리 없이 XML로 구성)

### 2. Networking (`core/network`)
- Retrofit + OkHttp + kotlinx.serialization
- `AuthInterceptor` — 저장된 Access Token을 자동 첨부 (로그인/리프레시 엔드포인트는 커스텀 헤더로 제외)
- `TokenAuthenticator` — 401 발생 시 Refresh Token으로 자동 갱신 후 1회 재시도, 실패 시 세션 만료 콜백 호출 (OkHttp의 동기 `Authenticator` 특성에 맞춰 iOS와는 다른 방식으로 구현)
- `apiCall{}` 헬퍼로 백엔드의 표준 에러 포맷(`{"error":{"code","message"}}`)을 모든 실패 케이스에서 동일하게 파싱

### 3. Auth 기반 (`core/auth`)
- `TokenStorage` — Android Keystore 기반 `EncryptedSharedPreferences` (iOS Keychain에 대응)
- `AuthSession` — `StateFlow<AuthState>`로 앱의 인증 상태(BOOTSTRAPPING/UNAUTHENTICATED/NEEDS_PROFILE_SETUP/AUTHENTICATED) 관리
- `SocialAuthProviding` — Naver/Google 로그인 SDK 추상화. iOS와 동일하게 **아직 실제 SDK는 연결하지 않은 스텁** (아래 백로그 참고)

### 4. Models/Services/Repositories (Auth + User 도메인)
- Backend Phase 1 API와 필드명이 정확히 일치 (`kotlinx.serialization`의 `@SerialName`으로 snake_case 매핑)

### 5. 디자인 시스템 (`ui/theme`, `ui/components`)
- Material3 기반 테마(브랜드 컬러 미정으로 임시 팔레트 사용, iOS와 동일한 이유)
- Primary/Secondary/Destructive/Text 버튼, `LoadingView`/`EmptyStateView`/`ErrorStateView`, Coil 기반 `RemoteImage`, `ComingSoonScreen`

### 6. 화면 흐름
- `SplashScreen` → `AuthSession.bootstrap()`
- `LoginScreen` → 네이버/구글 버튼 (스텁 호출, 에러 메시지 표시까지 실제 확인됨)
- `ProfileSetupScreen` → 닉네임 입력(2~30자 검증)
- `RootTabScaffold` → Navigation Compose 기반 5탭(홈/다이브로그/CO₂Table/커뮤니티/마이페이지), 홈·마이페이지만 실구현
- `AppRoot` → `AuthState`에 따라 위 화면 전환
- `AppContainer`(`DiveApplication`에서 생성) → 의존성 조립. iOS와 달리 DI 프레임워크(Hilt) 없이 수동 컨테이너로 구성

---

## 실제로 발견하고 고친 컴파일 에러 (실기기 검증의 가치)

컴파일러가 실제로 잡아준 문제들 — iOS 세션에서는 발견할 수 없었던 종류의 실수다.

1. `AppContainer`에서 프로퍼티 타입을 명시하지 않아 Kotlin 컴파일러가 "recursive type checking" 에러 발생 → 모든 프로�퍼티에 명시적 타입 지정
2. 존재하지 않는 `androidx.compose.foundation.layout.weight` import(실제로는 `ColumnScope`의 멤버 확장 함수라 import 자체가 불필요/불가능) → 제거
3. `TopAppBar` 등 Material3 실험적 API를 `@OptIn(ExperimentalMaterial3Api::class)` 없이 사용 → opt-in 추가
4. `by navController.currentBackStackEntryAsState()` 위임 구문에 `androidx.compose.runtime.getValue` import 누락 → 추가
5. `Modifier.padding(padding)`에 `androidx.compose.foundation.layout.padding` import 누락 → 추가
6. Deprecated 아이콘(`Icons.Filled.MenuBook` → `Icons.AutoMirrored.Filled.MenuBook`) → 경고 정리

---

## 설계 결정

1. **Naver/Google 로그인은 iOS와 동일하게 스텁으로 구현** — 실제 SDK 연동은 앱 등록(Client ID)이 필요해 이번 세션에서 진행하지 않았다.
2. **DI 프레임워크(Hilt/Koin) 대신 수동 `AppContainer`** — 앱 규모가 작아 프레임워크 도입의 이점보다 학습 비용이 커 보류. 도메인이 늘어나면 재검토 필요.
3. **API Base URL은 에뮬레이터 전용 주소(`10.0.2.2`)** — Android 에뮬레이터에서 호스트 PC의 `localhost`를 가리키는 특수 별칭이다. 실기기 테스트 시에는 PC의 실제 IP 또는 배포된 Render 주소로 바꿔야 한다.
4. **XcodeGen과 달리 Android는 표준 Gradle 프로젝트를 그대로 커밋** — Android 진영은 Gradle 프로젝트 파일 자체가 텍스트 기반이라 애초에 위험한 바이너리 프로젝트 파일 문제가 없다.

---

## 백로그 갱신

- [ ] Naver/Google 실제 로그인 SDK 연동 (iOS와 동일한 백로그 항목의 Android 버전)
- [ ] 실제 브랜드 컬러 확정 후 `ui/theme/Color.kt` 교체
- [ ] `ApiConfig.BASE_URL`을 빌드 변형(build variant)별로 분기해 Render 배포 주소 연결
- [ ] Play Store 배포를 위한 서명(keystore) 설정 및 Release 빌드 설정 (현재는 Debug 빌드만 검증됨)

---

## 다음 단계

- iOS는 Mac 확보 시 Phase 3 코드의 실제 빌드 검증 필요 (기존 백로그 최우선 항목 유지)
- Android는 이번에 확보한 실빌드 환경을 바탕으로 Phase 4(iOS)와 동일한 범위의 기능 화면(Dive Log, CO₂ Table, Certification, Community, Information/Banner)을 이어서 구현 가능
- 사용자 지시가 있을 때까지 다음 단계는 진행하지 않는다.
