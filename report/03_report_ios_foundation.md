# Phase 3 Report — iOS Frontend Foundation

## 요약

`Docs/09_Architecture.md`(Frontend Architecture), `Docs/08_DesignSystem.md`, `Docs/03_UserFlow.md`를 기준으로 SwiftUI 앱의 기반 구조를 구축했다. View → ViewModel → Repository → Service → API Client 계층을 확립하고, Auth 도메인(Splash → Login → Profile Setup → Home)을 백엔드 Phase 1 API와 실제로 연동되는 형태로 구현했으며, 공통 디자인 시스템 컴포넌트와 5탭 네비게이션 뼈대를 만들었다.

**⚠️ 중요한 제약**: 이 작업 환경(Windows)에는 Xcode/Swift 툴체인이 없어 **코드를 컴파일하거나 시뮬레이터에서 실행해 검증할 수 없었다.** 모든 파일은 괄호/중괄호 균형 검사와 수동 코드 리뷰(actor 격리 규칙, SwiftUI API 시그니처, 백엔드 스키마와의 필드명 일치)로만 검증했다. **반드시 Mac + Xcode 환경에서 최초 빌드 확인이 필요하다.**

---

## 구현 범위

### 1. 프로젝트 구성
- `Frontend/project.yml` — [XcodeGen](https://github.com/yonaskolb/XcodeGen) 스펙. Mac에서 `xcodegen generate` 실행 시 `.xcodeproj`를 재현 가능하게 생성한다. `.xcodeproj`를 직접 손으로 작성해 커밋하지 않은 이유는 아래 "설계 결정" 참고.
- `09_Architecture.md`의 Frontend Project Structure(App/Components/Features/Shared/Resources/Services/Repositories/Models/Utilities)를 그대로 반영한 폴더 구조

### 2. Networking (`Shared/Networking`)
- `APIClient` — 제네릭 `send<T: Decodable>` / `send(_:)`, 401 응답 시 Refresh Token으로 1회 재시도 후 실패하면 로그아웃 처리
- `Endpoint`, `HTTPMethod`, `APIError` — 백엔드의 표준 에러 응답(`{"error": {"code","message"}}`)을 그대로 파싱
- `JSONCoding` — snake_case ↔ camelCase 자동 변환, fractional seconds 유무에 관계없이 ISO8601 날짜 디코딩

### 3. Auth 기반 (`Shared/Auth`)
- `KeychainHelper` / `TokenStorage` — Access/Refresh Token을 Keychain에 저장
- `AuthSession` — `ObservableObject` + `AccessTokenProviding` 프로토콜을 동시에 구현하여 APIClient의 토큰 공급자 역할까지 수행. 앱의 인증 상태(`bootstrapping`/`unauthenticated`/`needsProfileSetup`/`authenticated`)를 단일 소스로 관리
- `SocialAuthProvider` — Naver/Google 네이티브 로그인 SDK를 감싸는 프로토콜. **실제 SDK는 아직 연결하지 않았고**, 호출 시 `SocialAuthError.sdkNotConfigured`를 던지는 스텁으로 구현 (아래 백로그 참고)

### 4. Models/Services/Repositories (Auth + User 도메인만, Phase 1 API와 1:1 대응)
- `Models/{Enums,AuthModels,UserModels}.swift` — 백엔드 Pydantic 스키마와 필드명이 정확히 일치하도록 작성 (예: `account_status` ↔ `accountStatus`)
- `Services/{AuthService,UserService}.swift` — Endpoint 정의 및 APIClient 호출 (로그인, 토큰 갱신, 로그아웃, 내 정보 조회, 프로필 설정)
- `Repositories/{AuthRepository,UserRepository}.swift` — 토큰 저장/삭제 등 도메인 로직을 서비스 위에 얹은 계층

### 5. 디자인 시스템 (`Shared/Theme`, `Shared/Components`)
- Theme: `AppColor`(Primary/Secondary/Background/Surface/Error/Warning/Success/Information — 실제 브랜드 컬러는 미정이라 시스템 컬러로 대체, 아래 백로그 참고), `AppTypography`(Dynamic Type 지원), `AppSpacing`/`AppCornerRadius`
- Components: Primary/Secondary/Destructive/Text 버튼 스타일, `LoadingView`, `EmptyStateView`, `ErrorStateView`, `RemoteImageView`(AsyncImage + placeholder/loading/error), `ComingSoonView`(Phase 4 전 임시 화면)

### 6. 화면 흐름
- `SplashView` → `AuthSession.bootstrap()`으로 저장된 세션 확인
- `LoginView` → 네이버/구글 로그인 버튼 (SDK 스텁 호출)
- `ProfileSetupView` → 최초 로그인 시 닉네임 입력 (2~30자 검증)
- `RootTabView` → 홈 / 다이브 로그 / CO₂ Table / 커뮤니티 / 마이페이지 5탭. 홈과 마이페이지(로그아웃 포함)만 실제 구현하고 나머지 3탭은 `ComingSoonView` 플레이스홀더
- `AppRootView` → `AuthSession.state`에 따라 위 화면들을 전환
- `DiveAppApp` → 의존성 조립(Composition Root): APIClient → Service → Repository → AuthSession, `APIClient.tokenProvider = authSession` 연결

---

## 설계 결정

1. **`.xcodeproj`를 직접 생성하지 않고 XcodeGen(`project.yml`)을 사용** — 이 환경에는 Xcode가 없어 `.xcodeproj`(pbxproj) 파일이 실제로 유효한지 검증할 방법이 없다. 잘못된 프로젝트 파일을 커밋하는 것이 아예 없는 것보다 위험하다고 판단해, 텍스트 기반이라 안전하게 작성/리뷰 가능한 XcodeGen 스펙을 대신 채택했다. Mac에서 `brew install xcodegen && cd Frontend && xcodegen generate`로 프로젝트를 생성한다.
2. **Naver/Google 로그인은 SDK 연결 전 스텁으로 구현** — 실제 SDK(NaverThirdPartyLogin/GoogleSignIn-iOS)는 SPM 의존성 추가와 실제 앱 등록(Client ID)이 필요해, 이 세션에서 연결해도 검증이 불가능하다. `SocialAuthProviding` 프로토콜로 추상화해두어 실제 SDK 연동 시 두 클래스의 구현부만 교체하면 되도록 설계했다.
3. **디자인 시스템 색상은 시스템 색상(semantic system colors)으로 임시 대체** — `08_DesignSystem.md`에 "실제 색상값은 추후 정의한다"고 명시되어 있어, 라이트/다크 모드를 모두 지원하는 시스템 색상을 자리표시자로 사용했다. 실제 브랜드 컬러가 정해지면 `AppColor.swift` 한 파일만 교체하면 된다.
4. **API Base URL은 `http://localhost:8000`으로 하드코딩(`AppConfig.swift`)** — Render 배포 URL이 아직 없어 로컬 개발 서버를 기본값으로 설정했다. 실제 배포 시 빌드 설정(Debug/Release)별 URL 분기가 필요하다.

---

## 검증

- 전체 40개 Swift 파일의 중괄호/괄호 균형 자동 검사 통과
- 수동 코드 리뷰로 다음을 확인:
  - `AccessTokenProviding`을 `@MainActor` 프로토콜로 선언하고 `AuthSession`이 이를 구현 — APIClient의 cross-actor 호출 패턴이 Swift 동시성 규칙과 일치하는지 확인
  - 모든 네트워크 모델의 필드명이 Backend Phase 1의 Pydantic 스키마와 정확히 일치하는지 대조 확인 (엔드포인트 경로, snake_case↔camelCase 변환 포함)
  - SwiftUI API 사용(`@ViewBuilder` 안에서의 `switch`, `AsyncImagePhase` 처리, `@FocusState`, `.buttonStyle` 커스텀 스타일 확장 등)이 표준 패턴과 일치하는지 확인
- **미수행**: 실제 Xcode 빌드, 시뮬레이터 실행, UI 동작 확인 — Mac 환경 필요 (아래 백로그에 최우선 항목으로 기록)

---

## 생성 파일 (Frontend/)

- `project.yml`
- `DiveApp/App/{DiveAppApp,AppRootView}.swift`
- `DiveApp/Shared/Networking/*.swift` (7개 파일)
- `DiveApp/Shared/Auth/*.swift` (4개 파일)
- `DiveApp/Shared/Theme/*.swift`, `DiveApp/Shared/Components/*.swift` (총 9개 파일)
- `DiveApp/Models/*.swift`, `DiveApp/Services/*.swift`, `DiveApp/Repositories/*.swift` (7개 파일)
- `DiveApp/Features/{Splash,Auth,Root,Home,DiveLog,Training,Community,MyPage}/*.swift` (13개 파일)
- `DiveApp/Resources/Assets.xcassets/` (Contents.json, AppIcon.appiconset)

---

## 백로그 갱신

`Report/00_todo_backlog.md`에 아래 항목을 최우선으로 추가했다.

- **Mac + Xcode에서 최초 빌드/실행 확인 필요 (최우선)** — 이 세션은 컴파일 검증을 전혀 하지 못했다.
- Naver/Google 실제 로그인 SDK 연동 (SPM 추가, 앱 등록, Client ID 설정)
- 실제 브랜드 컬러/타이포그래피 값 정의 후 `AppColor`/`AppTypography` 교체
- Release 빌드 설정에서 API Base URL을 Render 배포 주소로 분기 처리

---

## 다음 단계 (Phase 4 예정)

- Dive Log, CO₂ Table, Certification, Community, Information/Banner 화면을 `07_Screens.md` 목록에 따라 실제 구현 (현재 `ComingSoonView`로 남겨둔 3개 탭 포함)
- 사용자 지시가 있을 때까지 Phase 4는 진행하지 않는다.
