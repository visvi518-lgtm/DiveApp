# Project

## 프로젝트명

DiveApp

---

# 프로젝트 소개

DiveApp은 프리다이버와 스쿠버다이버를 위한 iOS 애플리케이션이다.

사용자는 다이브 로그를 기록하고, 자격증을 관리하며, CO₂ Table 훈련을 수행하고, 커뮤니티를 통해 정보를 공유할 수 있다.

본 프로젝트는 실제 서비스 출시를 목표로 개발한다.

---

# 프로젝트 목표

- 직관적이고 사용하기 쉬운 UI/UX 제공
- 실제 서비스 수준의 품질 구현
- 유지보수가 쉬운 구조 설계
- 확장 가능한 아키텍처 구축
- 재사용 가능한 컴포넌트 중심 개발

---

# 대상 사용자

- 프리다이버
- 스쿠버다이버
- 다이빙 강사
- 다이빙 교육기관

---

# 기술 스택

## Frontend

- Swift
- SwiftUI
- Swift Playground

## Backend

- Python
- FastAPI

## Database

- PostgreSQL (Render)

## Deployment

- Render

## API

- REST API
- Naver Login API
- Google Login API
- Naver Search API
- Naver Map API

---

# Architecture

- MVVM
- Repository Pattern
- Service Layer

---

# 개발 원칙

- 기능보다 구조를 우선한다.
- 유지보수성을 최우선으로 고려한다.
- 확장 가능한 구조를 설계한다.
- 코드 중복을 최소화한다.
- SOLID 원칙을 최대한 따른다.
- Apple Human Interface Guidelines(HIG)를 준수한다.
- SwiftUI 최신 개발 방식을 따른다.

---

# 프로젝트 범위

## 포함

- 로그인
- 다이브 로그
- CO₂ Table
- 자격증 관리
- 커뮤니티
- 관리자 페이지
- 정보 게시판
- 상단 배너 관리

## 제외(현재)

- Apple Watch
- Apple Health
- 오프라인 동기화
- 실시간 채팅

---

# 품질 목표

- 안정성
- 성능
- 보안
- 확장성
- 유지보수성
- 사용자 경험

---

# 코드 품질 기준

- 단일 책임 원칙(SRP)
- 함수는 하나의 역할만 수행
- View에는 UI만 작성
- 비즈니스 로직은 ViewModel과 Service에서 처리
- 하드코딩 금지
- 상수 분리
- 에러 처리 구현
- 재사용 가능한 컴포넌트 작성

---

# 배포 환경

Backend

- Render

Database

- PostgreSQL

환경 변수

- .env 관리

---

# 프로젝트 진행 원칙

기능을 구현하기 전에 반드시 다음 순서를 따른다.

1. 요구사항 분석
2. UI 설계
3. 데이터베이스 설계
4. API 설계
5. 구현
6. 테스트
7. 문서 업데이트
8. Git Commit

모든 기능은 위 절차를 따른다.
