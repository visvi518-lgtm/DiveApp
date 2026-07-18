# Report 10 — 이메일/비밀번호 로그인·회원가입 (Web)

## 배경

사용자가 Web 로그인 화면을 확인해보니 Naver/Google 소셜 로그인만 있고 일반적인 이메일/비밀번호 로그인·회원가입이 없다는 걸 발견, 추가를 요청했다. DB 스키마 변경(비밀번호 컬럼 추가, `AuthProvider` enum 확장)이 들어가는 작업이라 설계를 먼저 간단히 설명하고 진행했다. Web만 대상으로 했고(Android/iOS는 이번 범위 밖), 백엔드 API 자체는 플랫폼 중립적이라 나중에 이식 가능하다.

---

## 백엔드 설계

### 기존 구조 확인
`app/services/auth_service.py`, `app/routers/auth.py`, `app/models/user.py` 등을 먼저 읽어 기존 소셜 로그인 흐름(토큰 발급 + `is_new_user` 플래그로 신규가입 여부를 구분해 프로필 설정 화면으로 유도하는 패턴)을 파악하고, 이메일 인증도 **완전히 같은 패턴**을 재사용하도록 설계했다 — 새로운 UX를 만들지 않고 기존 `TokenResponse`/`is_new_user` 계약 그대로 회원가입 직후 프로필 설정으로 자연스럽게 이어지게 했다.

### 스키마 변경
- `AuthProvider` enum에 `EMAIL` 추가 (`NAVER`, `GOOGLE`, `EMAIL`)
- `User` 테이블에 `password_hash` 컬럼 추가 (nullable — 소셜 계정은 항상 NULL)
- 이메일 계정의 `provider_user_id`는 이메일 주소 자체를 사용 (기존 `UniqueConstraint(provider, provider_user_id)`를 그대로 활용해 이메일 중복 가입 방지)

새 마이그레이션 `0002_add_email_auth.py` 작성. **주의점**: PostgreSQL은 `ALTER TYPE ... ADD VALUE`를 트랜잭션 안에서 실행할 수 없어서, Alembic의 `op.get_context().autocommit_block()`으로 감싸야 했다 (이 프로젝트 첫 마이그레이션 이후 처음 만든 추가 마이그레이션이라 이 패턴이 새로 생김). downgrade에서는 enum 값 제거가 PostgreSQL에서 지원되지 않아 컬럼 삭제만 하고 enum은 그대로 둔다고 주석으로 명시했다.

### 새 엔드포인트
- `POST /api/v1/auth/register` — 이메일+비밀번호(8자 이상)로 가입, 항상 `is_new_user=true`
- `POST /api/v1/auth/login/email` — 이메일+비밀번호로 로그인

**라우트 순서 주의**: 기존 `POST /login/{provider}`가 동적 경로라서, `/login/email`을 그 **앞에** 선언해야 했다. FastAPI/Starlette는 라우트를 선언 순서대로 매칭하기 때문에, 순서를 반대로 하면 `/login/email` 요청이 `/login/{provider}`에 먼저 구조적으로 매칭되어(그 다음 `provider` enum 파싱에서 실패해) 422가 나버린다.

### 보안
- 비밀번호는 `bcrypt`로 해싱 (평문 저장 없음)
- 이메일 형식은 Pydantic `EmailStr` (`email-validator` 패키지 신규 추가)로 검증
- 로그인 실패 시 "이메일 또는 비밀번호가 올바르지 않습니다"라는 동일한 메시지를 반환 — 존재하지 않는 이메일인지, 비밀번호가 틀렸는지, 혹은 소셜 계정이라 비밀번호 자체가 없는지 구분해서 알려주지 않음 (계정 존재 여부가 노출되는 것을 막기 위한 의도적 설계)

---

## Web 구현

- `LoginPage.tsx`를 이메일/비밀번호 입력 폼 + "로그인 ↔ 회원가입" 전환 버튼 + 기존 Naver/Google 버튼을 함께 보여주는 형태로 재구성. 기존 디자인 시스템 클래스(`form-field`, `form-error`)를 그대로 재사용했다.
- `AuthContext`에 `registerWithEmail`/`loginWithEmail`을 추가하되, 토큰 저장 + `is_new_user` 분기 로직이 기존 `login`과 완전히 동일해서 `applyTokenResponse`라는 공통 헬퍼로 뽑아 3개 메서드가 공유하도록 정리했다.
- `models/enums.ts`의 `AuthProvider` 유니언 타입에 `'EMAIL'`을 추가하면서, 이를 소비하던 `adminFormat.ts`의 `authProviderLabel`(삼항 연산자로 NAVER/GOOGLE만 처리하던 함수)도 함께 고쳐 "이메일"이 정상 표시되도록 했다 — 안 고쳤으면 이메일 가입 회원이 Admin 회원 목록에서 "구글"로 잘못 표시될 뻔했다.

---

## 실제 검증 중 발견한 버그 2건 (기존 코드, 이번에 처음 발견)

실제 브라우저로 회원가입 → 프로필 설정 → 홈 → 로그아웃 → 재로그인까지 전체 사이클을 처음부터 끝까지 클릭해본 것은 이번이 처음이었다(이전 리포트들은 인증 우회나 이미 프로필이 있는 기존 계정으로만 테스트했음). 그 과정에서 소셜 로그인에도 원래 있었던 라우팅 버그 2개를 발견했다:

1. **`/profile-setup` 화면에 라우트 가드가 없었음**: 프로필 저장 API가 성공해도 그 화면 자체에서 다른 곳으로 이동시키는 로직이 없어서, 저장 후에도 계속 프로필 설정 화면에 머물러 있었다. `RequireProfileSetup` 가드를 추가해 `state === 'authenticated'`가 되면 자동으로 홈으로 이동하도록 수정.
2. **`/login` 화면(`PublicOnly` 가드)이 `needsProfileSetup` 상태를 처리하지 않았음**: 신규 가입 직후 상태가 `needsProfileSetup`으로 바뀌어도 `/login` 화면의 가드는 `authenticated`일 때만 리다이렉트해서, 로그인 화면에 그대로 멈춰 있었다. 조건을 추가해 `needsProfileSetup`일 때도 `/profile-setup`으로 이동하도록 수정.

두 버그 모두 소셜 로그인(Naver/Google)의 신규가입 플로우에도 동일하게 존재했던 문제로, 이번에 이메일 회원가입을 실제 브라우저로 처음부터 끝까지 테스트하다가 우연히 드러났다. 수정 후 Naver/Google 신규가입도 자동으로 함께 고쳐진 셈이다.

---

## 실제 검증

### Backend (curl)
- 회원가입 성공 → 토큰 발급, `is_new_user: true` 확인
- 동일 이메일 재가입 → 409 확인
- 로그인 성공 → 토큰 발급, `is_new_user: false` 확인
- 오답 비밀번호 → 401 확인
- 짧은 비밀번호(3자) 회원가입 → 422 확인
- 잘못된 이메일 형식 회원가입 → 422 확인
- 기존 소셜 로그인 라우트(`/login/GOOGLE`)가 라우트 순서 변경 후에도 정상 동작(가짜 토큰으로 401) 확인 — 새 라우트 추가가 기존 기능을 깨지 않았음을 확인

### Web (headless Chrome, 실제 백엔드)
- 회원가입 폼 렌더링 스크린샷 확인
- 회원가입 → 실제 DB에 계정 생성 → `/profile-setup`으로 자동 이동 확인
- 프로필(닉네임) 저장 → 홈(`/`)으로 자동 이동, 닉네임이 화면에 표시되는 것까지 확인
- 로그아웃 → `/login`으로 이동 확인
- 같은 이메일/비밀번호로 재로그인 → 홈으로 정상 진입 확인 (비밀번호 해시 검증이 실제로 동작함을 증명)
- 테스트 중 중복 닉네임으로 인한 409 에러 메시지("Nickname already in use")가 화면에 그대로, 크래시 없이 표시되는 것도 확인 (테스트 스크립트 실수로 우연히 발견한 부수 확인)

### 환경 이슈: 포터블 PostgreSQL 타임존 파일 유실
마이그레이션을 실행하려는 중, 이 세션에서 계속 써온 포터블 PostgreSQL에 대한 새 연결 시도가 전부 `ConnectionResetError`로 실패하는 문제가 발생했다(단, 이미 떠 있던 uvicorn의 기존 커넥션은 계속 정상 동작). 원인을 좁혀가다 보니 실제로는 SSL 문제가 아니라, `share/timezonesets/Default` 파일이 이 세션 중 언젠가 빈 디렉터리가 되어 있어서 — Postgres를 재시작하면 그 즉시 `FATAL: invalid value for parameter "timezone_abbreviations"`로 죽는 상태였다(기존에 떠 있던 프로세스는 최초 기동 때 이미 로드해뒀던 상태라 문제가 드러나지 않았을 뿐). GitHub의 PostgreSQL 16.4 공식 소스에서 해당 파일을 다시 받아 제자리에 복원한 뒤 정상적으로 재기동했고, 기존 DB 데이터(회원 1명 등)는 전부 그대로 보존되어 있음을 확인했다.

---

## 백로그 갱신

- [x] Web 이메일/비밀번호 로그인·회원가입 — 완료 (본 리포트)
- [x] `/profile-setup`, `/login` 라우트 가드 버그 2건 발견 및 수정
- [ ] Android/iOS에 동일 기능 이식 — 이번엔 범위 밖
- [ ] 이메일 인증, 비밀번호 재설정 — 이메일 발송 인프라 필요, 범위 밖
- [ ] 포터블 PostgreSQL의 `share/` 디렉터리가 왜 일부 비워졌는지는 원인 불명 — 재발 시 같은 방식(GitHub 공식 소스에서 파일 복원)으로 대응 가능

---

## 다음 단계

사용자 지시 순서의 4번("기타")은 아직 미정. 이번 요청은 그 전에 발견된 별도 수정 사항이었으므로, 이어서 무엇을 할지는 다음 대화에서 확인 필요.
