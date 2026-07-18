# Feature Map

## Core Features

```text
Authentication
│
├── Naver Login
├── Google Login
├── Auto Login
├── Logout
└── Dormant Account
```

---

```text
Dive Log
│
├── Freediving
│   ├── Write
│   ├── Edit
│   ├── Delete
│   ├── Photos
│   ├── Location
│   └── Statistics
│
└── Scuba
    ├── Write
    ├── Edit
    ├── Delete
    ├── Photos
    ├── Location
    └── Statistics
```

---

```text
CO₂ Table
│
├── Program
├── Timer
├── History
└── Statistics
```

---

```text
Certification
│
├── AIDA
├── PADI
├── Register
├── Edit
└── Delete
```

---

```text
Community
│
├── Board
├── Post
├── Comment
├── Search
└── My Posts
```

---

```text
Information
│
├── Freediving
├── Scuba
├── Search
└── Categories
```

---

```text
Banner
│
├── Image
├── Link
├── Order
└── Schedule
```

---

```text
Admin
│
├── Dashboard
├── User
├── Community
├── Information
├── Banner
└── Audit Log
```

---

## Feature Dependencies

```text
Authentication
      │
      ├──────────────┐
      ▼              ▼
 Dive Log       Community
      │              │
      ├──────┐       │
      ▼      ▼       ▼
Certificate  CO₂   Information
      │
      ▼
 My Page

Admin
│
├── User
├── Community
├── Information
├── Banner
└── Statistics
```

---

## MVP Features

- Authentication
- Dive Log
- CO₂ Table
- Certification
- Community
- Information
- Banner
- Admin
- My Page

---

## Future Features

- AI
- Apple Watch
- Apple Health
- Push Notification
- Dive Map
- Report System
- Apple Login
- Offline Mode
