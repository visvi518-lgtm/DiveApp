# Entity Relationship Diagram (ERD)

## Core Entities

- User
- UserProfile
- DiveLog
- DivePhoto
- DiveLocation
- TrainingRecord
- Certificate
- CommunityPost
- CommunityComment
- InformationArticle
- Banner
- AdminLog

---

## Relationships

User
├── 1 : N DiveLog
│
│ ├── 1 : 1 FreedivingLog
│ └── 1 : 1 ScubaLog
│
├── 1 : N TrainingRecord
├── 1 : N Certificate
├── 1 : N CommunityPost
└── 1 : N CommunityComment

DiveLog
├── 1 : N DivePhoto
└── N : 1 DiveLocation

CommunityPost
└── 1 : N CommunityComment

AdminLog
└── N : 1 User

---

## Entity Overview

### User

사용자 계정

---

### UserProfile

프로필 정보

---

### DiveLog

프리다이빙 / 스쿠버다이빙 로그

---

### DivePhoto

다이브 로그 사진

- id
- dive_log_id
- image_url
- display_order
- created_at
- updated_at
- deleted_at

---

### DiveLocation

다이빙 위치 정보

---

### TrainingRecord

CO₂ Table 훈련 기록

completed_sets
is_completed
rest_time_seconds

---

### Certificate

자격증 정보

---

### CommunityPost

커뮤니티 게시글

---

### CommunityComment

커뮤니티 댓글

---

### InformationArticle

정보 게시글

---

### Banner

메인 배너

---

### AdminLog

관리자 작업 로그

### FreedivingLog

프리다이빙 전용 데이터

---

### ScubaLog

스쿠버다이빙 전용 데이터
