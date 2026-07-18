# User Flow

## Flow Overview

모든 사용자 흐름은 아래 원칙을 따른다.

- 최소한의 화면 이동
- 일관된 Navigation
- 뒤로가기 시 데이터 유지
- 사용자가 현재 위치를 항상 인지할 수 있어야 한다.

---

# 1. Authentication

```text
Splash
    ↓
Auto Login Check
    ↓
┌───────────────┐
│ Login Success │─────────────┐
└───────────────┘             │
                              ▼
                           Home
                              ▲
┌───────────────┐             │
│ Login Screen  │─────────────┘
└───────────────┘
        │
        ▼
Naver Login / Google Login
        │
        ▼
First Login?
   │          │
 Yes         No
  │           │
  ▼           ▼
Profile Setup Home
```

---

# 2. Dive Log

```text
Home
    ↓
Dive Log
    ↓
Select Type
    ├── Freediving
    └── Scuba
            ↓
Write Log
    ↓
Save
    ↓
Detail
    ↓
Edit
    ↓
Update
```

---

# 3. CO₂ Table

```text
Home
    ↓
CO₂ Table
    ↓
Program Setting
    ↓
Training
    ↓
Result
    ↓
History
```

---

# 4. Certification

```text
Home
    ↓
Certification
    ↓
Certificate List
    ↓
Add Certificate
    ↓
Detail
    ↓
Edit
```

---

# 5. Community

```text
Home
    ↓
Community
    ↓
Board
    ↓
Post List
    ↓
Post Detail
    ↓
Comment
```

---

# 6. Information

```text
Home
    ↓
Information
    ↓
Category
    ↓
Article List
    ↓
Article Detail
```

---

# 7. My Page

```text
Home
    ↓
My Page
    ↓
Profile
    ↓
Edit Profile
```

---

# 8. Admin

```text
Admin Login
    ↓
Dashboard
    ↓
├── User Management
├── Community
├── Information
├── Banner
└── Statistics
```

---

# Navigation Principles

- 모든 화면은 Home으로 복귀 가능해야 한다.
- 뒤로가기는 사용자의 이전 작업 흐름을 유지한다.
- 작성 중인 데이터는 임시 저장을 고려한다.
- 삭제 작업은 반드시 확인 절차를 거친다.
