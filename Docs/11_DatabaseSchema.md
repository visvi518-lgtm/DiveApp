# Database Schema

## Overview

이 문서는 DiveApp의 실제 데이터베이스 테이블 구조를 정의한다.

데이터 타입, 네이밍 규칙, Enum 규칙은 `06_DataDictionary.md`를 따른다.

ERD는 `10_ERD.md`를 기준으로 한다.

---

## Table Relationships

```text
User
├── UserProfile (1:1)
├── DiveLog (1:N)
├── TrainingRecord (1:N)
├── Certificate (1:N)
├── CommunityPost (1:N)
├── CommunityComment (1:N)
└── AdminLog (1:N)

DiveLog
├── DiveLocation (N:1)
├── FreedivingLog (1:1)
├── ScubaLog (1:1)
└── DivePhoto (1:N)

CommunityPost
└── CommunityComment (1:N)

InformationArticle

Banner
```

---

## Entity Definitions

# User

## Purpose

사용자의 인증(Authentication)과 계정 정보를 관리한다.

프로필 정보는 UserProfile에서 관리하며, User는 인증과 권한에 필요한 최소 정보만 가진다.

---

## Columns

| Column           | Type          | PK  | FK  | Null | Unique | Default           | Description      |
| ---------------- | ------------- | --- | --- | ---- | ------ | ----------------- | ---------------- |
| id               | UUID          | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key      |
| provider         | AuthProvider  | -   | -   | ❌   | ❌     | -                 | OAuth Provider   |
| provider_user_id | VARCHAR(255)  | -   | -   | ❌   | ❌     | -                 | Provider User ID |
| email            | VARCHAR(255)  | -   | -   | ❌   | ✅     | -                 | Email            |
| role             | UserRole      | -   | -   | ❌   | ❌     | USER              | User Role        |
| account_status   | AccountStatus | -   | -   | ❌   | ❌     | ACTIVE            | Account Status   |
| last_login_at    | TIMESTAMPTZ   | -   | -   | ✅   | ❌     | NULL              | Last Login       |
| created_at       | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Created Time     |
| updated_at       | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Updated Time     |
| deleted_at       | TIMESTAMPTZ   | -   | -   | ✅   | ❌     | NULL              | Soft Delete      |
| email_verified   | BOOLEAN       | -   | -   | ❌   | ❌     | FALSE             | Email Verified   |
| is_pinned        | BOOLEAN       | -   | -   | ❌   | ❌     | FALSE             | Pinned Post      |

---

## Constraints

- UNIQUE(email)
- UNIQUE(provider, provider_user_id)

---

## Relationships

- User → UserProfile (1:1)
- User → DiveLog (1:N)
- User → TrainingRecord (1:N)
- User → Certificate (1:N)
- User → CommunityPost (1:N)
- User → CommunityComment (1:N)
- User → AdminLog (1:N)

---

## Indexes

- email
- (provider, provider_user_id)
- account_status

---

## Notes

- 이메일은 소셜 로그인 기준으로 관리한다.
- 인증(Authentication) 외의 개인정보는 UserProfile에서 관리한다.
- 일반(Local) 로그인 추가 시에도 User 테이블을 변경하지 않고 확장 가능하도록 설계한다.
- 이메일은 저장 전에 소문자로 정규화한다.

# DiveLocation

## Purpose

다이빙 위치 정보를 관리한다.

동일한 다이빙 포인트를 여러 로그에서 재사용할 수 있도록 독립된 엔티티로 관리한다.

---

## Columns

| Column         | Type          | PK  | FK  | Null | Unique | Default           | Description    |
| -------------- | ------------- | --- | --- | ---- | ------ | ----------------- | -------------- |
| id             | UUID          | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key    |
| name           | VARCHAR(100)  | -   | -   | ❌   | ❌     | -                 | Dive Site Name |
| address        | VARCHAR(255)  | -   | -   | ✅   | ❌     | NULL              | Address        |
| latitude       | DECIMAL(10,7) | -   | -   | ❌   | ❌     | -                 | Latitude       |
| longitude      | DECIMAL(10,7) | -   | -   | ❌   | ❌     | -                 | Longitude      |
| naver_place_id | VARCHAR(100)  | -   | -   | ✅   | ❌     | NULL              | Naver Place ID |
| created_at     | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Created Time   |
| updated_at     | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Updated Time   |
| deleted_at     | TIMESTAMPTZ   | -   | -   | ✅   | ❌     | NULL              | Soft Delete    |
| country        | VARCHAR(100)  | -   | -   | ✅   | ❌     | NULL              | Country        |
| city           | VARCHAR(100)  | -   | -   | ✅   | ❌     | NULL              | City           |

---

## Constraints

- latitude BETWEEN -90 AND 90
- longitude BETWEEN -180 AND 180
- UNIQUE(name, city, country)

---

## Relationships

- DiveLocation → DiveLog (1:N)

---

## Indexes

- name
- city
- (latitude, longitude)

---

## Validation

- 위치명 필수
- 위도/경도 필수
- 좌표 범위 검증

---

## Business Rules

- 동일한 위치는 가능한 재사용한다.
- 위치 정보 수정 시 해당 위치를 참조하는 로그에는 영향을 주지 않는다.

---

## Security

- 위치 데이터는 사용자가 직접 입력하거나 Naver Map API를 통해 선택한다.
- 좌표는 서버에서 검증한다.

---

## Future Expansion

- 국가
- 지역
- 수심 정보
- 대표 이미지
- 평점
- 다이빙 포인트 상세 정보

---

## Notes

동일한 다이빙 포인트를 여러 사용자가 공유할 수 있도록 설계한다.
사용자별 위치를 생성하지 않고 공용 위치 데이터를 재사용한다.

# UserProfile

## Purpose

사용자의 프로필 정보를 관리한다.

인증 정보는 User 테이블에서 관리하며, UserProfile은 사용자에게 표시되는 정보와 개인정보를 관리한다.

---

## Columns

| Column            | Type        | PK  | FK  | Null | Unique | Default           | Description       |
| ----------------- | ----------- | --- | --- | ---- | ------ | ----------------- | ----------------- |
| id                | UUID        | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key       |
| user_id           | UUID        | -   | ✅  | ❌   | ✅     | -                 | User ID           |
| nickname          | VARCHAR(30) | -   | -   | ❌   | ✅     | -                 | Nickname          |
| profile_image_url | TEXT        | -   | -   | ✅   | ❌     | NULL              | Profile Image URL |
| phone_number      | VARCHAR(20) | -   | -   | ✅   | ❌     | NULL              | Phone Number      |
| bio               | TEXT        | -   | -   | ✅   | ❌     | NULL              | Introduction      |
| created_at        | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Created Time      |
| updated_at        | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Updated Time      |
| deleted_at        | TIMESTAMPTZ | -   | -   | ✅   | ❌     | NULL              | Soft Delete       |

---

## Constraints

- UNIQUE(user_id)
- UNIQUE(nickname)

---

## Relationships

- UserProfile → User (1:1)

---

## Indexes

- user_id
- nickname

---

## Notes

- UserProfile는 사용자당 하나만 존재한다.
- 프로필 이미지는 URL만 저장한다.
- 자격증 정보는 Certificate 테이블에서 관리한다.

# DiveLog

## Purpose

사용자의 다이빙 활동에 대한 공통 정보를 관리한다.

프리다이빙와 스쿠버다이빙의 공통 데이터만 저장하며,
종목별 상세 데이터는 각각 FreedivingLog와 ScubaLog에서 관리한다.

---

## Columns

| Column      | Type          | PK  | FK  | Null | Unique | Default           | Description      |
| ----------- | ------------- | --- | --- | ---- | ------ | ----------------- | ---------------- |
| id          | UUID          | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key      |
| user_id     | UUID          | -   | ✅  | ❌   | ❌     | -                 | User ID          |
| location_id | UUID          | -   | ✅  | ❌   | ❌     | -                 | Dive Location ID |
| dive_type   | DiveType      | -   | -   | ❌   | ❌     | -                 | Dive Type        |
| dive_date   | DATE          | -   | -   | ❌   | ❌     | -                 | Dive Date        |
| latitude    | DECIMAL(10,7) | -   | -   | ✅   | ❌     | NULL              | Actual Latitude  |
| longitude   | DECIMAL(10,7) | -   | -   | ✅   | ❌     | NULL              | Actual Longitude |
| memo        | TEXT          | -   | -   | ✅   | ❌     | NULL              | Dive Memo        |
| created_at  | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Created Time     |
| updated_at  | TIMESTAMPTZ   | -   | -   | ❌   | ❌     | now()             | Updated Time     |
| deleted_at  | TIMESTAMPTZ   | -   | -   | ✅   | ❌     | NULL              | Soft Delete      |

---

## Constraints

- user_id NOT NULL
- location_id NOT NULL
- dive_type NOT NULL
- dive_date NOT NULL
- latitude BETWEEN -90 AND 90 (NULL 허용)
- longitude BETWEEN -180 AND 180 (NULL 허용)

---

## Relationships

- DiveLog → User (N:1)
- DiveLog → DiveLocation (N:1)
- DiveLog → FreedivingLog (1:1)
- DiveLog → ScubaLog (1:1)
- DiveLog → DivePhoto (1:N)

---

## Indexes

- user_id
- location_id
- dive_date
- dive_type
- (user_id, dive_date)

# FreedivingLog

## Purpose

프리다이빙 전용 데이터를 관리한다.

DiveLog의 공통 정보와 분리하여 프리다이빙에 필요한 데이터만 저장한다.

---

## Columns

| Column            | Type         | PK  | FK  | Null | Unique | Default           | Description         |
| ----------------- | ------------ | --- | --- | ---- | ------ | ----------------- | ------------------- |
| id                | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key         |
| dive_log_id       | UUID         | -   | ✅  | ❌   | ✅     | -                 | DiveLog ID          |
| max_depth         | DECIMAL(5,2) | -   | -   | ❌   | ❌     | -                 | Maximum Depth (m)   |
| dive_time_seconds | INTEGER      | -   | -   | ❌   | ❌     | -                 | Dive Time (seconds) |
| created_at        | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Created Time        |
| updated_at        | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Updated Time        |
| deleted_at        | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Soft Delete         |

---

## Constraints

- dive_log_id NOT NULL
- UNIQUE(dive_log_id)
- max_depth >= 0
- dive_time_seconds >= 0

---

## Relationships

- FreedivingLog → DiveLog (1:1)

---

## Indexes

- dive_log_id
- max_depth

---

## Notes

- DiveLog가 FREEDIVING인 경우에만 FreedivingLog가 생성된다.
- 하나의 DiveLog는 하나의 FreedivingLog만 가질 수 있다.
- 프리다이빙 전용 데이터만 저장한다.

# ScubaLog

## Purpose

스쿠버다이빙 전용 데이터를 관리한다.

DiveLog의 공통 정보와 분리하여 스쿠버다이빙에 필요한 데이터만 저장한다.

---

## Columns

| Column              | Type         | PK  | FK  | Null | Unique | Default           | Description               |
| ------------------- | ------------ | --- | --- | ---- | ------ | ----------------- | ------------------------- |
| id                  | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key               |
| dive_log_id         | UUID         | -   | ✅  | ❌   | ✅     | -                 | DiveLog ID                |
| max_depth           | DECIMAL(5,2) | -   | -   | ❌   | ❌     | -                 | Maximum Depth (m)         |
| dive_time_seconds   | INTEGER      | -   | -   | ❌   | ❌     | -                 | Dive Time (seconds)       |
| tank_pressure_start | INTEGER      | -   | -   | ❌   | ❌     | -                 | Tank Pressure Start (bar) |
| tank_pressure_end   | INTEGER      | -   | -   | ❌   | ❌     | -                 | Tank Pressure End (bar)   |
| created_at          | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Created Time              |
| updated_at          | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Updated Time              |
| deleted_at          | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Soft Delete               |

---

## Constraints

- dive_log_id NOT NULL
- UNIQUE(dive_log_id)
- max_depth >= 0
- dive_time_seconds >= 0
- tank_pressure_start >= 0
- tank_pressure_end >= 0
- tank_pressure_start >= tank_pressure_end

---

## Relationships

- ScubaLog → DiveLog (1:1)

---

## Indexes

- dive_log_id
- max_depth

---

## Notes

- DiveLog가 SCUBA인 경우에만 ScubaLog가 생성된다.
- 하나의 DiveLog는 하나의 ScubaLog만 가질 수 있다.
- 스쿠버다이빙 전용 데이터만 저장한다.
- 탱크 압력은 bar 단위로 저장한다.

# DivePhoto

## Purpose

다이브 로그에 첨부되는 사진을 관리한다.

하나의 DiveLog에는 여러 장의 사진을 등록할 수 있다.

---

## Columns

| Column        | Type        | PK  | FK  | Null | Unique | Default           | Description   |
| ------------- | ----------- | --- | --- | ---- | ------ | ----------------- | ------------- |
| id            | UUID        | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key   |
| dive_log_id   | UUID        | -   | ✅  | ❌   | ❌     | -                 | DiveLog ID    |
| image_url     | TEXT        | -   | -   | ❌   | ❌     | -                 | Image URL     |
| display_order | INTEGER     | -   | -   | ❌   | ❌     | 1                 | Display Order |
| created_at    | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Created Time  |
| updated_at    | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Updated Time  |
| deleted_at    | TIMESTAMPTZ | -   | -   | ✅   | ❌     | NULL              | Soft Delete   |

---

## Constraints

- dive_log_id NOT NULL
- image_url NOT NULL
- display_order >= 1
- UNIQUE(dive_log_id, display_order)

---

## Relationships

- DivePhoto → DiveLog (N:1)

---

## Indexes

- dive_log_id
- display_order

---

## Notes

- 하나의 DiveLog에는 여러 장의 사진을 등록할 수 있다.
- 사진의 노출 순서는 display_order로 관리한다.
- 실제 이미지 파일은 DB가 아닌 스토리지에 저장하고, DB에는 URL만 저장한다.

# TrainingRecord

## Purpose

사용자의 CO₂ Table 훈련 기록을 관리한다.

훈련 프로그램 정보와 실제 수행 결과를 저장하여
훈련 이력 및 통계 기능에 활용한다.

---

## Columns

| Column                | Type        | PK  | FK  | Null | Unique | Default           | Description              |
| --------------------- | ----------- | --- | --- | ---- | ------ | ----------------- | ------------------------ |
| id                    | UUID        | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key              |
| user_id               | UUID        | -   | ✅  | ❌   | ❌     | -                 | User ID                  |
| total_sets            | INTEGER     | -   | -   | ❌   | ❌     | -                 | Total Sets               |
| completed_sets        | INTEGER     | -   | -   | ❌   | ❌     | 0                 | Completed Sets           |
| is_completed          | BOOLEAN     | -   | -   | ❌   | ❌     | FALSE             | Training Completed       |
| rest_time_seconds     | INTEGER     | -   | -   | ❌   | ❌     | -                 | Initial Rest Time        |
| hold_time_seconds     | INTEGER     | -   | -   | ❌   | ❌     | -                 | Initial Hold Time        |
| rest_interval_seconds | INTEGER     | -   | -   | ❌   | ❌     | -                 | Rest Time Change Per Set |
| hold_interval_seconds | INTEGER     | -   | -   | ❌   | ❌     | -                 | Hold Time Change Per Set |
| completed_at          | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Training Completed Time  |
| created_at            | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Created Time             |
| updated_at            | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Updated Time             |
| deleted_at            | TIMESTAMPTZ | -   | -   | ✅   | ❌     | NULL              | Soft Delete              |

---

## Constraints

- user_id NOT NULL
- total_sets >= 5
- total_sets <= 20
- completed_sets >= 0
- completed_sets <= total_sets
- rest_time_seconds > 0
- hold_time_seconds > 0

---

## Relationships

- TrainingRecord → User (N:1)

---

## Indexes

- user_id
- completed_at
- is_completed

---

## Notes

- CO₂ Table 훈련 기록만 저장한다.
- 프로그램 설정값과 실제 완료 결과를 함께 저장한다.
- 통계는 TrainingRecord 데이터를 기반으로 계산한다.
- O₂ Table은 향후 별도 테이블 또는 TrainingType 확장으로 지원한다.

# Certificate

## Purpose

사용자가 보유한 다이빙 자격증 정보를 관리한다.

AIDA, PADI 등 다양한 교육 기관의 자격증을 저장하며,
향후 다른 기관도 쉽게 확장할 수 있도록 설계한다.

---

## Columns

| Column                | Type                      | PK  | FK  | Null | Unique | Default           | Description                |
| --------------------- | ------------------------- | --- | --- | ---- | ------ | ----------------- | -------------------------- |
| id                    | UUID                      | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key                |
| user_id               | UUID                      | -   | ✅  | ❌   | ❌     | -                 | User ID                    |
| organization          | CertificationOrganization | -   | -   | ❌   | ❌     | -                 | Certification Organization |
| certification_level   | VARCHAR(100)              | -   | -   | ❌   | ❌     | -                 | Certification Level        |
| certification_number  | VARCHAR(100)              | -   | -   | ✅   | ❌     | NULL              | Certification Number       |
| issue_date            | DATE                      | -   | -   | ✅   | ❌     | NULL              | Issue Date                 |
| expiration_date       | DATE                      | -   | -   | ✅   | ❌     | NULL              | Expiration Date            |
| instructor            | VARCHAR(100)              | -   | -   | ✅   | ❌     | NULL              | Instructor Name            |
| dive_center           | VARCHAR(100)              | -   | -   | ✅   | ❌     | NULL              | Dive Center                |
| certificate_image_url | TEXT                      | -   | -   | ✅   | ❌     | NULL              | Certificate Image URL      |
| memo                  | TEXT                      | -   | -   | ✅   | ❌     | NULL              | Memo                       |
| created_at            | TIMESTAMPTZ               | -   | -   | ❌   | ❌     | now()             | Created Time               |
| updated_at            | TIMESTAMPTZ               | -   | -   | ❌   | ❌     | now()             | Updated Time               |
| deleted_at            | TIMESTAMPTZ               | -   | -   | ✅   | ❌     | NULL              | Soft Delete                |

---

## Constraints

- user_id NOT NULL
- organization NOT NULL
- certification_level NOT NULL

---

## Relationships

- Certificate → User (N:1)
- expiration_date IS NULL OR issue_date IS NULL OR expiration_date >= issue_date

---

## Indexes

- user_id
- organization
- certification_level

---

## Notes

- 한 명의 사용자는 여러 개의 자격증을 등록할 수 있다.
- 동일한 교육 기관의 여러 단계(Level)를 보유할 수 있다.
- 자격증 이미지는 URL만 저장한다.

# CommunityPost

## Purpose

사용자가 커뮤니티에 작성하는 게시글을 관리한다.

MVP에서는 단일 게시판으로 운영하며,
향후 게시판 카테고리를 확장할 수 있도록 설계한다.

---

## Columns

| Column        | Type         | PK  | FK  | Null | Unique | Default           | Description   |
| ------------- | ------------ | --- | --- | ---- | ------ | ----------------- | ------------- |
| id            | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key   |
| user_id       | UUID         | -   | ✅  | ❌   | ❌     | -                 | Author ID     |
| title         | VARCHAR(200) | -   | -   | ❌   | ❌     | -                 | Post Title    |
| content       | TEXT         | -   | -   | ❌   | ❌     | -                 | Post Content  |
| view_count    | INTEGER      | -   | -   | ❌   | ❌     | 0                 | View Count    |
| like_count    | INTEGER      | -   | -   | ❌   | ❌     | 0                 | Like Count    |
| comment_count | INTEGER      | -   | -   | ❌   | ❌     | 0                 | Comment Count |
| is_pinned     | BOOLEAN      | -   | -   | ❌   | ❌     | FALSE             | Pinned Post   |
| created_at    | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Created Time  |
| updated_at    | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Updated Time  |
| deleted_at    | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Soft Delete   |

---

## Constraints

- user_id NOT NULL
- title NOT NULL
- content NOT NULL
- view_count >= 0
- like_count >= 0
- comment_count >= 0

---

## Relationships

- CommunityPost → User (N:1)
- CommunityPost → CommunityComment (1:N)

---

## Indexes

- user_id
- is_pinned
- created_at
- view_count

---

## Notes

- MVP에서는 단일 게시판으로 운영한다.
- 게시판 카테고리는 향후 확장 기능으로 추가한다.
- 좋아요 수와 댓글 수는 성능 향상을 위해 캐시 값으로 저장한다.

# CommunityComment

## Purpose

사용자가 게시글에 작성한 댓글을 관리한다.

MVP에서는 일반 댓글만 지원하며,
대댓글(답글)은 향후 확장 기능으로 추가할 수 있도록 설계한다.

---

## Columns

| Column     | Type        | PK  | FK  | Null | Unique | Default           | Description       |
| ---------- | ----------- | --- | --- | ---- | ------ | ----------------- | ----------------- |
| id         | UUID        | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key       |
| post_id    | UUID        | -   | ✅  | ❌   | ❌     | -                 | Community Post ID |
| user_id    | UUID        | -   | ✅  | ❌   | ❌     | -                 | Author ID         |
| content    | TEXT        | -   | -   | ❌   | ❌     | -                 | Comment Content   |
| created_at | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Created Time      |
| updated_at | TIMESTAMPTZ | -   | -   | ❌   | ❌     | now()             | Updated Time      |
| deleted_at | TIMESTAMPTZ | -   | -   | ✅   | ❌     | NULL              | Soft Delete       |

---

## Constraints

- post_id NOT NULL
- user_id NOT NULL
- content NOT NULL

---

## Relationships

- CommunityComment → CommunityPost (N:1)
- CommunityComment → User (N:1)

---

## Indexes

- post_id
- created_at

---

## Notes

- MVP에서는 일반 댓글만 지원한다.
- 대댓글(답글)은 향후 parent_comment_id를 추가하여 확장한다.
- 댓글 삭제는 Soft Delete를 사용한다.

# InformationArticle

## Purpose

관리자가 다이빙과 관련된 정보를 사용자에게 제공하기 위한 콘텐츠를 관리한다.

MVP에서는 공지, 교육 자료, 안전 정보 등 읽기 전용 콘텐츠를 제공한다.

---

## Columns

| Column              | Type         | PK  | FK  | Null | Unique | Default           | Description         |
| ------------------- | ------------ | --- | --- | ---- | ------ | ----------------- | ------------------- |
| id                  | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key         |
| title               | VARCHAR(200) | -   | -   | ❌   | ❌     | -                 | Article Title       |
| content             | TEXT         | -   | -   | ❌   | ❌     | -                 | Article Content     |
| thumbnail_image_url | TEXT         | -   | -   | ✅   | ❌     | NULL              | Thumbnail Image URL |
| view_count          | INTEGER      | -   | -   | ❌   | ❌     | 0                 | View Count          |
| is_published        | BOOLEAN      | -   | -   | ❌   | ❌     | FALSE             | Published Status    |
| published_at        | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Published Time      |
| created_at          | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Created Time        |
| updated_at          | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Updated Time        |
| deleted_at          | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Soft Delete         |

---

## Constraints

- title NOT NULL
- content NOT NULL
- view_count >= 0

---

## Relationships

- 없음

---

## Indexes

- is_published
- published_at
- created_at

---

## Notes

- 관리자만 작성 및 수정할 수 있다.
- 일반 사용자는 공개된(is_published = TRUE) 게시글만 조회할 수 있다.
- 조회수는 성능 향상을 위해 캐시 값으로 저장한다.

# Banner

## Purpose

앱 메인 화면 및 주요 화면에 노출되는 배너를 관리한다.

MVP에서는 관리자가 배너를 등록·수정·삭제할 수 있으며,
사용자는 활성화된 배너만 조회할 수 있다.

---

## Columns

| Column        | Type         | PK  | FK  | Null | Unique | Default           | Description        |
| ------------- | ------------ | --- | --- | ---- | ------ | ----------------- | ------------------ |
| id            | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key        |
| title         | VARCHAR(100) | -   | -   | ❌   | ❌     | -                 | Banner Title       |
| image_url     | TEXT         | -   | -   | ❌   | ❌     | -                 | Banner Image URL   |
| banner_type   | BannerType   | -   | -   | ❌   | ❌     | -                 | Banner Type        |
| target_url    | TEXT         | -   | -   | ✅   | ❌     | NULL              | Target URL         |
| display_order | INTEGER      | -   | -   | ❌   | ❌     | 1                 | Display Order      |
| is_active     | BOOLEAN      | -   | -   | ❌   | ❌     | TRUE              | Active Status      |
| start_at      | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Display Start Time |
| end_at        | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Display End Time   |
| created_at    | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Created Time       |
| updated_at    | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Updated Time       |
| deleted_at    | TIMESTAMPTZ  | -   | -   | ✅   | ❌     | NULL              | Soft Delete        |

---

## Constraints

- title NOT NULL
- image_url NOT NULL
- banner_type NOT NULL
- display_order >= 1

---

## Relationships

- 없음

---

## Indexes

- is_active
- display_order
- start_at
- end_at

---

## Notes

- 활성화된(is_active = TRUE) 배너만 사용자에게 노출한다.
- 노출 기간은 start_at과 end_at을 기준으로 판단한다.
- 동일한 display_order에서는 생성 순(created_at)으로 정렬한다.
- 배너 이미지는 URL만 저장한다.

# AdminLog

## Purpose

관리자의 주요 작업 이력을 기록한다.

운영 중 발생한 변경 사항을 추적하고 감사(Audit)를 위한 로그를 저장한다.

---

## Columns

| Column        | Type         | PK  | FK  | Null | Unique | Default           | Description           |
| ------------- | ------------ | --- | --- | ---- | ------ | ----------------- | --------------------- |
| id            | UUID         | ✅  | -   | ❌   | ✅     | gen_random_uuid() | Primary Key           |
| admin_user_id | UUID         | -   | ✅  | ❌   | ❌     | -                 | Administrator User ID |
| action        | VARCHAR(100) | -   | -   | ❌   | ❌     | -                 | Action Name           |
| target_type   | VARCHAR(100) | -   | -   | ❌   | ❌     | -                 | Target Entity Type    |
| target_id     | UUID         | -   | -   | ✅   | ❌     | NULL              | Target Entity ID      |
| description   | TEXT         | -   | -   | ✅   | ❌     | NULL              | Action Description    |
| ip_address    | VARCHAR(45)  | -   | -   | ✅   | ❌     | NULL              | IP Address            |
| created_at    | TIMESTAMPTZ  | -   | -   | ❌   | ❌     | now()             | Action Time           |

---

## Constraints

- admin_user_id NOT NULL
- action NOT NULL
- target_type NOT NULL

---

## Relationships

- AdminLog → User (N:1)

---

## Indexes

- admin_user_id
- action
- target_type
- created_at

---

## Notes

- 관리자(Role = ADMIN)의 작업만 기록한다.
- 감사(Audit) 및 운영 이력 추적을 위한 테이블이다.
- 로그 데이터는 수정하지 않으며, 필요한 경우만 보관 정책에 따라 삭제 또는 보관한다.
- admin_user_id는 role = ADMIN인 사용자만 참조할 수 있다.
