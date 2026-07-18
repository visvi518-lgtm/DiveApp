# Domain Model

## Core Domains

### User

사용자를 관리하는 도메인

Responsibilities

- Authentication
- Profile
- Certification
- Account Status

---

### DiveLog

다이빙 기록을 관리하는 도메인

Responsibilities

- Freediving Log
- Scuba Log
- Photos
- Location
- Statistics

---

### Training

훈련 기능을 관리하는 도메인

Responsibilities

- CO₂ Table
- History
- Statistics

---

### Community

커뮤니티를 관리하는 도메인

Responsibilities

- Board
- Post
- Comment

---

### Information

정보 게시글을 관리하는 도메인

Responsibilities

- Categories
- Articles

---

### Banner

배너를 관리하는 도메인

Responsibilities

- Banner
- Schedule

---

### Admin

관리 기능을 담당하는 도메인

Responsibilities

- User Management
- Community Management
- Banner Management
- Information Management
- Audit Log

---

## Domain Relationships

User
│
├── DiveLog
├── Training
├── Certification
└── Community

Community
└── Comment

Admin
├── User
├── Community
├── Information
└── Banner
