# DiveAppSwiftPlayground 구현 리포트

## 1. 목적

`DiveAppSwiftPlayground`는 기존 DiveApp 전체 앱을 iPad Swift Playgrounds에서 가볍게 확인하기 위한 Mock 기반 SwiftUI 앱입니다.

실제 백엔드, 소셜 로그인, 토큰 저장, Keychain, 이미지 업로드 같은 무거운 기능은 제외하고, 핵심 화면 구조와 사용자 흐름을 빠르게 검토할 수 있도록 구성했습니다.

## 2. 구현한 기능

| 구분 | 구현 내용 | 파일 |
|---|---|---|
| 앱 진입점 | SwiftUI `@main` 앱 구성 | `Sources/DiveAppPlaygroundApp.swift` |
| 앱 상태 | 로그인/프로필 설정/로그인 완료 상태 관리 | `Sources/MockAppState.swift` |
| Mock 모델 | 사용자, 배너, 다이브 로그, 훈련 기록, 게시글, 자격증 모델 | `Sources/MockModels.swift` |
| 디자인 시스템 | 색상, 타이포그래피, spacing, corner radius | `Sources/DesignSystem.swift` |
| 공통 컴포넌트 | 버튼 스타일, EmptyState, MetricChip, BannerCard | `Sources/Components.swift` |
| 로그인 화면 | 샘플 계정 시작, 새 사용자 흐름 시작 | `Sources/LoginMockView.swift` |
| 프로필 설정 | 닉네임, 다이빙 레벨 입력 후 Mock 사용자 생성 | `Sources/ProfileSetupMockView.swift` |
| 메인 탭 | 홈, 로그, 훈련, 커뮤니티, 마이페이지 탭 | `Sources/MainTabMockView.swift` |
| 홈 | 사용자 인사, 로그 수, 누적 다이브, 배너, 일정 EmptyState | `Sources/HomeMockView.swift` |
| 다이브 로그 | 목록, 상세, 새 로그 Mock 추가 | `Sources/DiveLogMockView.swift` |
| 훈련 | CO2 Table 느낌의 Mock 타이머 UI, 최근 훈련 목록 | `Sources/TrainingMockView.swift` |
| 커뮤니티 | 인기 글 목록, 게시글 상세, Mock 댓글 | `Sources/CommunityMockView.swift` |
| 마이페이지 | 사용자 정보, 다이빙 레벨, 누적 다이브, 자격증 목록, 로그아웃 | `Sources/MyPageMockView.swift` |

## 3. 넣어도 무리 없는 기능

Swift Playgrounds에서 유지하기 적합한 기능입니다.

| 기능 | 적용 방식 |
|---|---|
| 탭 기반 화면 구조 | `TabView`로 구현 |
| 화면 이동 | `NavigationStack`, `NavigationLink` 사용 |
| 정적/Mock 목록 | 배열 기반 Mock 데이터 사용 |
| 폼 입력 | `TextField`, `Picker`, `Stepper` 사용 |
| 간단한 상태 전환 | `ObservableObject`, `@Published`, `@State` 사용 |
| 로그 추가 | 서버 저장 없이 메모리 배열에 추가 |
| 로그아웃 | Mock 상태를 signedOut으로 변경 |
| Empty/Placeholder 상태 | 서버 데이터 없음 상태를 UI로 표시 |

## 4. Mock으로 대체한 기능

| 원래 필요한 기능 | Playground 대체 방식 |
|---|---|
| Naver/Google 소셜 로그인 | 버튼 클릭 시 `MockAppState.login()` 실행 |
| 신규 사용자 판별 | 버튼 클릭 시 `needsProfileSetup` 상태로 이동 |
| 프로필 저장 API | 입력값으로 메모리 `currentUser` 갱신 |
| 다이브 로그 생성 API | 메모리 `diveLogs` 배열에 insert |
| 사용자 정보 조회 API | `MockUser.sample` 사용 |
| 배너 조회 API | `MockBanner.samples` 사용 |
| 훈련 기록 조회 API | `MockTrainingRecord.samples` 사용 |
| 커뮤니티 게시글 조회 API | `MockPost.samples` 사용 |
| 자격증 조회 API | `MockCertificate.samples` 사용 |
| 이미지 다운로드/업로드 | SF Symbols와 텍스트 상태로 대체 |

## 5. 실제 앱에 필요한 API

Swift Playgrounds Mock 버전에는 포함하지 않았지만, 실제 DiveApp 구현에는 필요한 API입니다.

| 영역 | API 예시 | 목적 |
|---|---|---|
| 인증 | `POST /auth/social-login` | Naver/Google 로그인 후 서버 세션 생성 |
| 인증 | `POST /auth/refresh` | 액세스 토큰 갱신 |
| 인증 | `POST /auth/logout` | 서버 측 로그아웃 또는 토큰 무효화 |
| 사용자 | `GET /users/me` | 현재 사용자 정보 조회 |
| 사용자 | `PATCH /users/me/profile` | 닉네임, 레벨 등 프로필 설정/수정 |
| 홈 | `GET /banners` | 홈 배너 조회 |
| 홈 | `GET /information/articles` | 정보성 콘텐츠 조회 |
| 다이브 로그 | `GET /dive-logs` | 다이브 로그 목록 조회 |
| 다이브 로그 | `POST /dive-logs` | 다이브 로그 생성 |
| 다이브 로그 | `GET /dive-logs/{id}` | 다이브 로그 상세 조회 |
| 다이브 로그 | `PATCH /dive-logs/{id}` | 다이브 로그 수정 |
| 다이브 로그 | `DELETE /dive-logs/{id}` | 다이브 로그 삭제 |
| 사진 | `POST /dive-logs/{id}/photos` | 다이브 사진 업로드 |
| 훈련 | `GET /training-records` | 훈련 기록 목록 조회 |
| 훈련 | `POST /training-records` | CO2 Table 등 훈련 기록 저장 |
| 커뮤니티 | `GET /community/posts` | 게시글 목록 조회 |
| 커뮤니티 | `POST /community/posts` | 게시글 작성 |
| 커뮤니티 | `GET /community/posts/{id}` | 게시글 상세 조회 |
| 커뮤니티 | `POST /community/posts/{id}/comments` | 댓글 작성 |
| 자격증 | `GET /certificates` | 사용자 자격증 목록 조회 |
| 자격증 | `POST /certificates` | 자격증 등록 |

## 6. 구현하지 않은 기능

Playground 실행 안정성과 경량화를 위해 제외한 기능입니다.

| 기능 | 제외 이유 |
|---|---|
| 실제 소셜 로그인 | 외부 SDK, URL scheme, 앱 설정, 서버 연동이 필요함 |
| 실제 백엔드 API 호출 | Playground 검토 목적에는 네트워크 계층이 과함 |
| Access Token 저장/갱신 | 인증 흐름을 Mock으로 대체했기 때문에 불필요 |
| Keychain 저장 | iPad Playground 검토용 앱에서는 복잡도 대비 이점이 낮음 |
| 이미지 업로드 | 파일 선택, 권한, multipart 업로드, 서버 저장이 필요함 |
| 원격 이미지 로딩 | URL, 캐시, 실패 상태 처리가 필요함 |
| Push 알림 | Apple Developer 설정과 권한 처리가 필요함 |
| 관리자 기능 | 사용자용 Playground 흐름과 목적이 다름 |
| 오프라인 영속 저장 | 현재는 메모리 상태만 사용함 |
| 실제 CO2 Table 타이머 로직 | UI 확인용 Mock 상태만 구현함 |

## 7. 앞으로 필요한 작업

| 우선순위 | 작업 |
|---|---|
| 높음 | iPad Swift Playgrounds에서 패키지 열림 여부 확인 |
| 높음 | `DiveAppSwiftPlayground` 폴더가 바로 열리지 않으면 `.swiftpm` 확장자 적용 |
| 중간 | 실제 앱의 디자인 시스템과 색상/spacing 동기화 |
| 중간 | 다이브 로그 작성 폼을 실제 필드 구조에 가깝게 확장 |
| 중간 | CO2 Table 타이머 Mock을 실제 라운드 구조에 가깝게 개선 |
| 낮음 | 샘플 이미지 asset 추가 |
| 낮음 | 다크 모드 화면 점검 |

## 8. 결론

현재 `DiveAppSwiftPlayground`는 실제 서비스 기능을 실행하는 앱이 아니라, iPad Swift Playgrounds에서 DiveApp의 핵심 UI 흐름을 검토하기 위한 경량 Mock 앱입니다.

탭 구조, 홈, 로그, 훈련, 커뮤니티, 마이페이지까지 화면 구현은 가능하며, 실제 로그인/서버/저장 기능은 본 앱에서만 유지하는 것이 적절합니다.
