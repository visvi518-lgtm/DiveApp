# Data Dictionary

## Naming Convention

### Table

- Singular PascalCase
- Example
  - User
  - DiveLog
  - Certification

---

### Column

- snake_case
- Example
  - created_at
  - updated_at
  - user_id

---

### Primary Key

모든 Primary Key는 UUID를 사용한다.

Example

- id UUID PRIMARY KEY

---

### Foreign Key

형식

- {table}\_id

Example

- user_id
- dive_log_id

---

### Date & Time

모든 시간은 UTC 기준으로 저장한다.

필드

- created_at
- updated_at
- deleted_at

---

### Boolean

접두사

- is\_
- has\_

Example

- is_active
- is_deleted
- has_certificate

---

### Enum

Enum을 사용하는 항목은 별도로 관리한다.

예시

AccountStatus

- ACTIVE
- DORMANT
- SUSPENDED
- DELETED

AuthProvider

- NAVER
- GOOGLE

BannerType

- NOTICE
- EVENT
- PROMOTION
- INFORMATION

CertificationOrganization

- AIDA
- PADI
- SSI
- CMAS
- RAID
- SDI
- NAUI

DiveType

- FREEDIVING
- SCUBA

UserRole

- USER
- ADMIN

---

### File Storage

이미지는 DB에 저장하지 않는다.

DB에는 아래 정보만 저장한다.

- file_name
- file_url
- file_size
- mime_type

---

### Soft Delete

삭제는 기본적으로 Soft Delete를 사용한다.

삭제 컬럼

- deleted_at

---

### Audit

주요 데이터는 아래 컬럼을 가진다.

- created_at
- updated_at
- deleted_at
- created_by (선택)
- updated_by (선택)
