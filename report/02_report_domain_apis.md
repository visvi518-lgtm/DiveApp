# Phase 2 Report — Domain APIs

## 요약

Phase 1에서 구축한 Router → Service → Repository 구조를 그대로 확장하여, MVP 핵심 도메인 6종(Dive Log, CO₂ Table, Certification, Community, Information, Banner)과 Admin(대시보드/회원 관리)을 구현했다. 새 DB 테이블은 추가하지 않고 `11_DatabaseSchema.md`에 이미 정의된 스키마 위에서 구현했다.

총 31개 API 엔드포인트가 등록되었으며, 모두 OpenAPI 스키마 생성과 구조적 스모크 테스트를 통과했다.

---

## 구현 범위

### 1. Dive Log (`/api/v1/dive-logs`)
- 생성 시 `DiveLocation`을 `(name, city, country)` 기준으로 재사용/신규 생성 (문서 "동일한 위치는 가능한 재사용한다" 반영)
- `dive_type`에 따라 `FreedivingLog`/`ScubaLog` 중 하나만 생성하도록 Pydantic 모델 검증 적용
- 사진 여러 장 등록/전체 교체(`replace_photos`), 목록 조회(종류/기간/지역 필터), 통계(총 다이빙 횟수/최대 수심/누적 시간/종목별 횟수)
- 소유자 본인 데이터만 조회/수정/삭제 가능 (Soft Delete)

### 2. CO₂ Table / Training (`/api/v1/trainings`)
- 완료된(혹은 중도 종료된) 훈련 기록 저장, 이력 조회, 통계(총 횟수/완료율/평균 완료 세트/최근 훈련일)
- `total_sets`(5~20), `completed_sets <= total_sets` 등 요구사항의 제약을 Pydantic 검증으로 선반영 (DB Check 제약과 이중 방어)
- 타이머 자체는 클라이언트(SwiftUI)에서 구동하고, 서버는 결과만 기록하는 구조로 설계 (`03_UserFlow.md` 흐름과 일치)

### 3. Certification (`/api/v1/certificates`)
- 등록/조회/수정/삭제(Soft Delete), 발급일-만료일 순서 검증

### 4. Community (`/api/v1/community`)
- 게시글 CRUD, 조회 시 조회수 자동 증가, 제목/내용 검색, "내가 쓴 글"(`/posts/mine`)
- 댓글 CRUD, 댓글 작성/삭제 시 게시글의 `comment_count` 캐시값 동기화
- 작성자 본인 또는 `ADMIN` 권한으로 삭제 가능. 관리자가 타인 게시글/댓글을 삭제하면 `AdminLog`에 자동 기록

### 5. Information (`/api/v1/information/articles`, `/api/v1/admin/information/articles`)
- 일반 사용자는 게시된(`is_published=true`) 글만 조회, 관리자는 작성/수정/삭제 및 미게시 글 포함 목록 조회
- 게시 상태로 전환되는 순간 `published_at` 자동 기록

### 6. Banner (`/api/v1/banners`, `/api/v1/admin/banners`)
- 일반 사용자는 활성 + 노출 기간 내의 배너만 `display_order` 순으로 조회
- 관리자 등록/수정/삭제, `display_order` 중복 방지 검증(서비스 레이어, 요구사항 3.7 Validation 반영 — DB에는 유니크 제약이 없어 애플리케이션 레벨에서 검증)

### 7. Admin (`/api/v1/admin`)
- 대시보드: 총 회원 수, 오늘 가입자 수, 활성 사용자 수, 게시글/댓글/다이브로그 수 집계
- 회원 목록 조회(이메일/닉네임 검색), 상세 조회, 계정 정지/정지 해제 — 모든 조작은 `AdminLog`에 기록

### 8. 공통
- 모든 관리자 작성/수정/삭제/정지 액션은 `AdminLog`에 기록되도록 서비스 레이어에 공통 반영 (`AdminLogRepository`)
- 새 DB 테이블/컬럼 없이 기존 스키마로 전체 구현 완료

---

## 검증

- 전체 신규 `.py` 파일 `py_compile` 통과
- `app.openapi()` 호출로 31개 엔드포인트 전체가 예외 없이 스키마로 직렬화됨을 확인 (Pydantic 모델 간 참조 오류, 라우터 등록 누락 등을 구조적으로 검증)
- `TestClient`로 대표 시나리오 실행:
  - 인증 없이 보호된 엔드포인트 호출 → 403
  - 잘못된 요청 본문(다이빙 종류에 안 맞는 상세정보 누락, CO₂ Table 세트 수 범위 위반, 빈 제목) → 422
  - 유효한 요청은 Repository까지 도달해 실제 PostgreSQL 연결 시도 단계에서만 실패 (로컬에 DB가 없어 예상된 결과) → 비즈니스 로직/검증 계층이 정상 동작함을 확인
  - `USER` role 토큰으로 관리자 전용 엔드포인트 호출 → 403 (Role 기반 인가 정상 동작)

실제 PostgreSQL/트랜잭션 동작(제약조건 위반 시 실제 에러, N+1 여부, 통계 쿼리의 실제 값 등)은 여전히 실제 DB 연결 후 통합 테스트가 필요하다 (Backlog 4번 항목 유지).

---

## 생성/수정 파일

- `Backend/app/models/{dive_log,community_post,community_comment}.py` (관계(relationship) 추가)
- `Backend/app/schemas/{dive_log,training,certificate,community,information,banner,admin}.py`
- `Backend/app/repositories/{dive_log_repository,training_repository,certificate_repository,community_repository,information_repository,banner_repository,admin_repository,admin_log_repository}.py`
- `Backend/app/repositories/user_repository.py` (관리자용 회원 검색 메서드 추가)
- `Backend/app/services/{dive_log_service,training_service,certificate_service,community_service,information_service,banner_service,admin_service}.py`
- `Backend/app/routers/{dive_log,training,certificate,community,information,banner,admin}.py`
- `Backend/app/main.py` (신규 라우터 등록)

---

## 백로그 갱신

`Report/00_todo_backlog.md`에 아래 항목을 추가로 기록했다. (상세 내용은 해당 문서 참고)

- Banner `display_order` 유니크 제약이 DB 스키마에 없어 애플리케이션 레벨에서만 검증 중 — 동시성 상황에서는 레이스 컨디션 가능성 있음. 필요 시 DB 유니크 제약 추가 검토.
- Community 게시글 이미지 첨부, Information 카테고리/출처, DiveLog 부가 필드(날씨/시야/버디 등), TrainingRecord Memo 등 Phase 1에서 발견한 스키마 갭은 여전히 미해결 (사용자 결정 대기 중)
- 실제 PostgreSQL 연결 통합 테스트, 실제 이미지 업로드 인프라 연동은 여전히 미수행

---

## 다음 단계 (Phase 3 예정)

- iOS(SwiftUI) 프론트엔드 프로젝트 뼈대 구축: API Client, 인증 흐름, 공통 디자인 시스템, 탭 네비게이션
- 사용자 지시가 있을 때까지 Phase 3는 진행하지 않는다.
