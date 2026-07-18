# DiveApp - 로컬 실행 가이드

## 사전 요구사항

- Python 3.11+
- PostgreSQL 14+
- Node.js 20+ (Web)
- Android Studio + JDK 17+ (Android, 선택)
- Xcode (iOS는 현재 보류 중 — 필요 없음)

---

## 1단계: PostgreSQL 설치 및 DB 생성

PostgreSQL이 이미 설치되어 있다면:

```bash
psql -U postgres -c "CREATE DATABASE diveapp;"
```

설치가 안 되어 있다면 [postgresql.org](https://www.postgresql.org/download/)에서 설치하거나, Docker가 있다면:

```bash
docker run --name diveapp-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

---

## 2단계: 백엔드 설정

```bash
cd Backend

# 가상환경 생성
python -m venv .venv
.venv\Scripts\activate      # Windows
# source .venv/bin/activate # macOS/Linux

# 패키지 설치
pip install -r requirements.txt

# 환경변수 설정
copy .env.example .env      # Windows
# cp .env.example .env      # macOS/Linux
```

`.env` 파일을 열어 아래 값을 확인/수정한다.

```env
DATABASE_URL=postgresql+asyncpg://postgres:postgres@localhost:5432/diveapp
JWT_SECRET_KEY=아무-랜덤-문자열로-변경
```

```bash
# DB 테이블 생성 (이미 작성된 마이그레이션 적용)
alembic upgrade head

# 서버 실행
uvicorn app.main:app --reload --port 8000
```

정상 기동되면 http://localhost:8000/health 에서 `{"status":"ok"}` 를 확인할 수 있고, http://localhost:8000/docs 에서 전체 API 문서(Swagger UI)를 볼 수 있다.

---

## 3단계-A: Web 프론트엔드 설정

```bash
cd Web
npm install
npm run dev
```

브라우저에서 http://localhost:5173 접속. 백엔드 주소는 기본값이 `http://localhost:8000`이며, 다른 주소를 쓰려면 `Web/.env` 파일에 `VITE_API_BASE_URL=http://your-backend-host:8000` 을 설정한다.

---

## 3단계-B: Android 앱 설정

### Android Studio로 실행 (권장)

1. Android Studio에서 `DiveApp/Android` 폴더 열기 (Gradle 동기화 자동 진행)
2. 에뮬레이터 생성 (Pixel 계열 권장) 또는 실기기 연결
3. Run ▶ 버튼으로 실행

### 커맨드라인으로 빌드만 하려면

```bash
cd Android
./gradlew assembleDebug
# 생성된 APK: Android/app/build/outputs/apk/debug/app-debug.apk
```

**백엔드 주소 관련 중요 사항** (`Android/app/src/main/java/com/diveapp/android/core/network/ApiConfig.kt`):

- 에뮬레이터에서 실행 시 `10.0.2.2:8000`이 개발 PC의 `localhost:8000`을 자동으로 가리키므로 별도 설정 없이 바로 연결된다.
- **실기기**에서 테스트하려면 PC의 LAN IP(예: `192.168.0.10`)로 `ApiConfig.BASE_URL`을 바꿔야 하고, `app/src/debug/res/xml/network_security_config.xml`에 그 IP도 `<domain>`으로 추가해야 한다 (Android 9+ 는 기본적으로 평문 HTTP 통신을 차단하며, 현재는 `10.0.2.2`/`localhost`/`127.0.0.1`만 허용되어 있다).

---

## 3단계-C: 실제 Naver/Google 로그인 설정 (Report 08)

Android/Web 모두 실제 로그인 SDK 연동 코드는 작성되어 있다 (Naver: NidOAuth SDK / Naver Login JS SDK, Google: Credential Manager / Google Identity Services). 다만 각자 발급받은 실제 Client ID/Secret을 아래처럼 채워 넣어야 동작한다. 채우지 않으면 두 버튼 모두 "OO 로그인이 설정되지 않았습니다"라는 안내 메시지만 뜨고 크래시 없이 종료된다.

### Google Cloud Console

1. OAuth 동의 화면을 구성한다.
2. **"웹 애플리케이션"** 유형 OAuth 클라이언트를 하나 만든다 → 이 Client ID를 Web과 Android 양쪽에서 공용으로 사용한다 (`VITE_GOOGLE_CLIENT_ID`, `GOOGLE_WEB_CLIENT_ID`). Android도 Credential Manager 아키텍처상 Web 클라이언트 ID를 `serverClientId`로 사용해야 백엔드가 검증 가능한 ID 토큰을 받는다.
3. **"Android"** 유형 OAuth 클라이언트도 별도로 등록해야 한다 (패키지명 `com.diveapp.android` + 서명 SHA-1). 이 클라이언트의 Client ID 값 자체는 코드에서 쓰이지 않지만, 등록이 없으면 로그인이 거부된다.
   - 디버그 키스토어 SHA-1(이 저장소의 `Android/app` 기준, 새로 빌드하면 기기마다 달라질 수 있으니 직접 재확인 권장):
     ```
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```

### Naver Developers

1. 애플리케이션을 등록하고 **Client ID / Client Secret**을 발급받는다.
2. **웹 서비스 URL / 콜백 URL**에 `http://localhost:5173/auth/naver/callback` (배포 시 실제 도메인도 추가)을 등록한다.
3. **Android 앱**을 추가로 등록한다 — 패키지명 `com.diveapp.android` (키 해시는 요구되지 않음).

### 값 채워넣기

**Web** (`Web/.env`, `Web/.env.example` 참고):

```env
VITE_GOOGLE_CLIENT_ID=발급받은 Web Client ID
VITE_NAVER_CLIENT_ID=발급받은 Naver Client ID
```

**Android** (`Android/local.properties`, `Android/local.properties.example` 참고):

```properties
NAVER_CLIENT_ID=발급받은 Naver Client ID
NAVER_CLIENT_SECRET=발급받은 Naver Client Secret
NAVER_CLIENT_NAME=DiveApp
GOOGLE_WEB_CLIENT_ID=발급받은 Web Client ID (Web과 동일한 값)
```

**Backend** (`Backend/.env`, 이미 있는 키 그대로 채우면 됨):

```env
NAVER_CLIENT_ID=발급받은 Naver Client ID
NAVER_CLIENT_SECRET=발급받은 Naver Client Secret
GOOGLE_CLIENT_ID=발급받은 Web Client ID
```

값을 채운 뒤에는 Web은 `npm run build`(또는 dev 서버 재시작), Android는 Gradle 동기화만 하면 반영된다.

---

## 4단계: iOS (현재 보류)

iOS는 이 저장소에 SwiftUI 코드(`Frontend/`)가 이미 작성되어 있지만, Mac/Xcode 환경에서 아직 빌드 확인을 하지 못한 상태다. Mac에서 재개하려면:

```bash
cd Frontend
brew install xcodegen   # 없다면
xcodegen generate
open DiveApp.xcodeproj
```

`Shared/Networking/AppConfig.swift`의 `apiBaseURL`을 개발 PC 주소로 맞춘 뒤 시뮬레이터에서 실행한다.

---

## 테스트 계정 만들기

**Web은 이제 이메일/비밀번호로 직접 회원가입할 수 있다** (Report 10) — `http://localhost:5173/login`에서 "계정이 없으신가요? 회원가입"을 누르고 이메일/비밀번호(8자 이상)만 입력하면 된다. Naver/Google 앱 등록 없이 가장 빠르게 로그인 상태를 확인하는 방법.

Android나 Admin 권한 계정처럼 이메일 회원가입만으로 부족한 경우(관리자 권한 부여 등)에는 아래처럼 DB에 직접 계정을 만들고 토큰을 발급받아 확인할 수 있다 (3단계-C의 OAuth 앱 등록 여부와 무관하게 항상 사용 가능한 방법).

```bash
cd Backend
.venv\Scripts\python.exe -c "
import asyncio
from app.core.security import create_access_token, create_refresh_token
from app.database.session import AsyncSessionLocal
from app.models.enums import AuthProvider
from app.models.user import User
from app.models.user_profile import UserProfile
from sqlalchemy import select

async def main():
    async with AsyncSessionLocal() as session:
        result = await session.execute(
            select(User).where(User.provider == AuthProvider.NAVER, User.provider_user_id == 'local-test')
        )
        user = result.scalar_one_or_none()
        if user is None:
            user = User(provider=AuthProvider.NAVER, provider_user_id='local-test', email='tester@example.com')
            session.add(user)
            await session.flush()
            session.add(UserProfile(user_id=user.id, nickname='테스트유저'))
            await session.commit()
            await session.refresh(user)
        print('ACCESS_TOKEN =', create_access_token(user.id, user.role.value))
        print('REFRESH_TOKEN =', create_refresh_token(user.id))

asyncio.run(main())
"
```

- **API를 직접 호출**할 때는 `ACCESS_TOKEN`을 `Authorization: Bearer <token>` 헤더로 사용한다.
- **앱(Android/Web)에 로그인 상태로 진입**하려면, 앱을 한 번 실행한 뒤 저장된 토큰 위치에 `REFRESH_TOKEN` 값을 넣어주면 된다 (Web은 브라우저 개발자 도구 콘솔에서 `localStorage.setItem('diveapp.refreshToken', '<REFRESH_TOKEN>')` 실행 후 새로고침, Android는 별도 디버그 진입점이 없으므로 백엔드 API를 직접 호출해 확인하거나, 실제 Naver/Google 앱을 등록해 3단계-C대로 로그인하는 것을 권장한다).
- 관리자 화면을 확인하려면 위 계정을 만든 뒤 DB에서 role을 바꾼다.

```bash
psql -U postgres -d diveapp -c "UPDATE \"User\" SET role = 'ADMIN' WHERE email = 'tester@example.com';"
```

---

## 나중에 추가할 기능

- [x] Naver/Google 로그인 SDK 연동 (Android: NidOAuth + Credential Manager, Web: Naver Login JS SDK + Google Identity Services) — 코드는 완료, 실제 Client ID/Secret 등록은 3단계-C 참고 (Report 08)
- [ ] iOS Naver/Google 로그인 SDK 연동 (iOS: NaverThirdPartyLogin, GoogleSignIn-iOS) — iOS 재개 시 진행
- [ ] Naver Map API 연동 (현재 다이빙 위치는 위도/경도 수동 입력)
- [ ] 이미지 업로드(다이브 사진, 자격증 이미지, 커뮤니티 이미지) — 현재 URL만 저장 가능
- [ ] Admin 대시보드 웹 화면 (API는 이미 있음)
- [ ] Render 배포 및 실제 프로덕션 환경변수 설정

전체 진행 상황과 알려진 이슈는 `Report/00_todo_backlog.md`와 번호가 매겨진 리포트 파일들을 참고한다.
