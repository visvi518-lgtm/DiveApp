# 로컬 실행 방법 (Backend / Web / Android)

더 자세한 사전 준비(PostgreSQL 설치, 테스트 계정 생성 등)는 `DiveApp/START.md`를 참고. 이 문서는 세 플랫폼을 빠르게 켜는 방법만 정리한다.

---

## 1. Backend (FastAPI)

사전 조건: PostgreSQL이 떠 있고 `Backend/.env`의 `DATABASE_URL`이 그 주소를 가리키고 있어야 함.

```bash
cd DiveApp/Backend
.venv\Scripts\activate          # Windows
uvicorn app.main:app --reload --port 8000
```

확인: `http://localhost:8000/health` → `{"status":"ok"}`, `http://localhost:8000/docs` → Swagger UI.

**포트 충돌 시**: `[WinError 10013]` 또는 `[WinError 10048]`이 뜨면 이미 다른 uvicorn 인스턴스가 8000번을 쓰고 있다는 뜻. PowerShell에서 확인/종료:
```powershell
Get-NetTCPConnection -LocalPort 8000 | Select OwningProcess
Stop-Process -Id <PID> -Force
```

---

## 2. Web (React + Vite)

```bash
cd DiveApp/Web
npm install       # 최초 1회
npm run dev
```

확인: `http://localhost:5173`. 기본적으로 `http://localhost:8000` 백엔드를 바라보며, 다른 주소를 쓰려면 `Web/.env`에 `VITE_API_BASE_URL`을 설정.

실제 로그인 없이 화면만 확인하려면 `START.md`의 "테스트 계정 만들기" 절차로 토큰을 발급받아 브라우저 콘솔에서:
```js
localStorage.setItem('diveapp.refreshToken', '<REFRESH_TOKEN>')
```
실행 후 새로고침.

---

## 3. Android

### Android Studio로 실행 (권장)
1. Android Studio에서 `DiveApp/Android` 폴더 열기 (Gradle 동기화 자동 진행)
2. 에뮬레이터 생성(Pixel 계열 권장) 또는 실기기 연결
3. Run ▶

### 커맨드라인으로 빌드만
```bash
cd DiveApp/Android
./gradlew assembleDebug
# 결과물: Android/app/build/outputs/apk/debug/app-debug.apk
```

에뮬레이터에서는 `10.0.2.2:8000`이 개발 PC의 `localhost:8000`을 자동으로 가리켜 별도 설정 없이 백엔드에 연결된다. 실기기로 테스트하려면 PC의 LAN IP로 `ApiConfig.BASE_URL`을 바꾸고 `app/src/debug/res/xml/network_security_config.xml`에도 그 IP를 추가해야 한다 (Android 9+ 평문 HTTP 차단 때문).

---

## 참고
- 전체 사전 준비/테스트 계정 생성/OAuth 앱 등록 절차: `DiveApp/START.md`
- 남은 작업/체크리스트: `Report/toDoList.md`
