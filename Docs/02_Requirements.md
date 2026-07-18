# Requirements

## 1. Project Overview

### Purpose

DiveApp은 프리다이빙과 스쿠버다이빙 사용자를 위한 iOS 애플리케이션이다.

사용자는 자신의 다이빙 활동을 기록하고, CO₂ 훈련을 수행하며, 자격증을 관리하고, 커뮤니티를 통해 정보를 공유할 수 있다.

---

### Objectives

- 쉽고 직관적인 다이빙 기록 서비스 제공
- 체계적인 CO₂ 훈련 지원
- 자격증 통합 관리
- 다이버 커뮤니티 제공
- 지속적으로 확장 가능한 플랫폼 구축

---

### Platforms

- iOS (Primary)

Backend API는 다양한 클라이언트에서 사용할 수 있도록 REST API로 설계한다.

---

### Target Release

- MVP
- Production Ready Architecture

## 2. Target Users

### Primary Users

#### Freedivers

- 다이브 로그를 기록하고 관리하려는 사용자
- CO₂ Table 훈련을 수행하는 사용자
- 자격증을 관리하려는 사용자

#### Scuba Divers

- 다이브 로그를 기록하는 사용자
- 다이빙 포인트를 저장하는 사용자
- 다이빙 사진을 관리하는 사용자

---

### Secondary Users

#### Dive Instructors

- 교육 정보를 공유하는 사용자
- 커뮤니티 활동을 하는 사용자

#### Dive Centers

- 공지 및 이벤트 정보를 제공하는 사용자
- 배너를 통해 정보를 홍보하는 사용자

---

### Administrator

관리자는 별도의 관리자 권한을 가지며 다음 기능을 수행한다.

- 사용자 관리
- 게시글 관리
- 배너 관리
- 정보글 관리
- 서비스 운영 관리

## 3. Functional Requirements

### 3.1 Authentication

#### Purpose

사용자가 안전하게 서비스를 이용할 수 있도록 인증 및 계정 관리 기능을 제공한다.

---

#### Features

##### Social Login

지원 로그인

- Naver Login (OAuth 2.0)
- Google Login (OAuth 2.0)

---

##### Account Management

지원 기능

- 회원가입(소셜 로그인 최초 1회)
- 로그인
- 로그아웃
- 자동 로그인
- Access Token 재발급
- Refresh Token 관리

---

##### Profile Setup

최초 로그인 시

- 닉네임 입력
- 프로필 이미지(선택)
- 이메일 확인
- 전화번호(선택)

---

##### Account Status

계정 상태

- Active
- Dormant
- Suspended
- Deleted

---

##### Dormant Account

- 장기 미접속 계정 휴면 처리
- 로그인 시 휴면 해제

---

##### Password

소셜 로그인만 사용하는 MVP에서는 비밀번호를 사용하지 않는다.

향후 일반 로그인(Local Account) 추가 시 비밀번호 재설정을 지원한다.

---

#### Validation

- 이메일 중복 방지
- 닉네임 중복 방지
- OAuth Token 검증
- 로그인 상태 검증

---

#### Security

- JWT 기반 인증
- Refresh Token 사용
- HTTPS 통신
- 민감 정보 암호화 저장

---

#### MVP Scope

포함

- Naver Login
- Google Login
- 자동 로그인
- 로그아웃
- 휴면 계정

제외

- Apple Login
- 일반 회원가입
- 2단계 인증(2FA)

### 3.2 Dive Log

#### Purpose

사용자가 프리다이빙 및 스쿠버다이빙 활동을 체계적으로 기록하고 관리할 수 있도록 한다.

---

#### Log Types

- Freediving Log
- Scuba Diving Log

두 로그는 동일한 구조를 최대한 재사용하며, 필요한 항목만 분리한다.

---

#### Common Information

모든 로그는 다음 정보를 저장한다.

- Dive Date
- Dive Site
- GPS Location
- Cover Image
- Dive Images (Multiple)
- Memo
- Visibility
- Water Temperature
- Weather
- Dive Buddy
- Dive Center
- Equipment
- Favorite
- Created At
- Updated At

---

#### Freediving Information

추가 기록 항목

- Max Depth
- Dive Time
- Water Type
- Weight
- Suit Thickness
- Fins Type
- Lanyard Used
- Safety Diver
- Training Type
- Feeling

---

#### Scuba Diving Information

추가 기록 항목

- Max Depth
- Bottom Time
- Tank Start Pressure
- Tank End Pressure
- Air Consumption
- Gas Type
- Weight
- Suit Type
- Suit Thickness
- Current
- Dive Boat
- Instructor
- Dive Type

---

#### Photo Management

지원 기능

- 다중 사진 업로드
- 사진 삭제
- 사진 순서 변경
- 대표 이미지 지정

---

#### Location

지원 기능

- Naver Map API 기반 위치 검색
- 지도에서 위치 선택
- GPS 좌표 저장
- 장소명 저장

---

#### Search

검색 조건

- 날짜
- 지역
- 로그 종류
- 즐겨찾기

---

#### Statistics

사용자 통계

- 총 다이빙 횟수
- 최대 수심
- 누적 다이빙 시간
- 프리다이빙 횟수
- 스쿠버다이빙 횟수

---

#### Validation

- 필수 항목 검증
- 이미지 업로드 검증
- GPS 데이터 검증

---

#### MVP Scope

포함

- 로그 작성
- 로그 수정
- 로그 삭제
- 사진 업로드
- 위치 저장
- 로그 검색
- 기본 통계

제외

- 지도 시각화
- 로그 공유
- PDF 내보내기
- 로그 가져오기/내보내기

### 3.3 CO₂ Table

#### Purpose

사용자가 체계적으로 CO₂ 내성 훈련을 수행하고 기록할 수 있도록 한다.

---

#### Training Type

MVP에서는 CO₂ Table만 제공한다.

향후 기능

- O₂ Table
- Static Training
- Dynamic Training

---

#### Default Program

기본값

- Total Sets : 8
- Rest Time : 2m 00s
- Breath Hold : 1m 00s

세트 진행 시

- Rest Time -15초
- Breath Hold +10초

---

#### Custom Program

사용자는 다음 항목을 변경할 수 있다.

- Set Count
- Rest Time
- Hold Time
- Rest Interval
- Hold Interval

---

#### Constraints

- 최소 세트 수 : 5
- 최대 세트 수 : 20

---

#### Timer

지원 기능

- 시작
- 일시정지
- 재시작
- 종료
- 현재 세트 표시
- 남은 시간 표시
- 전체 진행률 표시

---

#### Audio

지원 기능

- 시작 알림
- 종료 알림
- 진동
- 음성 안내(향후)

---

#### History

훈련 기록 저장

- Training Date
- Program
- Completed Sets
- Success 여부
- Memo

---

#### Statistics

통계

- 총 훈련 횟수
- 완료율
- 평균 완료 세트
- 최근 훈련일

---

#### Validation

- 최소 세트 검증
- 시간 값 검증
- 음수 입력 방지

---

#### MVP Scope

포함

- CO₂ Table
- 사용자 설정
- 타이머
- 훈련 기록
- 통계

제외

- O₂ Table
- Apple Watch 연동
- Apple Health 연동
- AI 추천 프로그램

### 3.4 Certification

#### Purpose

사용자가 보유한 다이빙 자격증을 등록하고 체계적으로 관리할 수 있도록 한다.

---

#### Supported Organizations

MVP 지원 기관

- AIDA
- PADI

향후 확장

- SSI
- RAID
- SDI
- NAUI
- CMAS
- 기타 기관

---

#### Certificate Information

저장 항목

- Organization
- Certification Level
- Certification Number (Optional)
- Issue Date (Optional)
- Expiration Date (Optional)
- Instructor (Optional)
- Dive Center (Optional)
- Certificate Image
- Memo

---

#### Certificate Management

지원 기능

- 자격증 등록
- 자격증 수정
- 자격증 삭제
- 자격증 이미지 변경
- 여러 개의 자격증 등록

---

#### Image

지원 기능

- 갤러리 선택
- 카메라 촬영
- 이미지 교체
- 이미지 삭제

---

#### Validation

- 기관 선택 필수
- 등급 선택 필수
- 이미지 형식 검증

---

#### MVP Scope

포함

- AIDA
- PADI
- 이미지 저장
- 자격증 관리

제외

- QR 인증
- 기관 API 연동
- 자격증 자동 검증

### 3.5 Community

#### Purpose

사용자 간 정보 공유와 소통을 위한 커뮤니티 기능을 제공한다.

---

#### Boards

MVP 게시판

- 자유게시판
- 프리다이빙
- 스쿠버다이빙

향후 추가

- 중고장터
- 질문/답변
- 다이빙 포인트
- 장비 리뷰

---

#### Post

지원 기능

- 게시글 작성
- 게시글 수정
- 게시글 삭제
- 게시글 조회
- 내가 작성한 글 조회

---

#### Post Contents

저장 항목

- 제목
- 내용
- 이미지(최대 10장)
- 작성자
- 작성일
- 수정일
- 조회수

---

#### Comment

지원 기능

- 댓글 작성
- 댓글 수정
- 댓글 삭제

향후 추가

- 대댓글
- 댓글 좋아요

---

#### Search

검색 조건

- 제목
- 내용
- 작성자

---

#### Report

MVP

- 신고 기능 제외

향후 추가

- 게시글 신고
- 댓글 신고

---

#### Permission

일반 사용자

- 게시글 작성
- 수정(본인 글)
- 삭제(본인 글)
- 댓글 작성

관리자

- 모든 게시글 삭제
- 모든 댓글 삭제
- 게시글 작성 제한 관리

---

#### Validation

- 빈 제목 금지
- 빈 내용 금지
- 이미지 형식 검증
- 이미지 개수 제한

---

#### MVP Scope

포함

- 게시글 CRUD
- 댓글 CRUD
- 내가 작성한 글
- 이미지 첨부
- 검색

제외

- 좋아요
- 북마크
- 신고
- 대댓글
- 알림

### 3.6 Information

#### Purpose

사용자에게 프리다이빙 및 스쿠버다이빙 관련 최신 정보를 제공한다.

---

#### Categories

- Freediving
- Scuba Diving

---

#### Data Source

관리자가 직접 작성하거나, Naver Search API를 활용하여 수집한 정보를 기반으로 게시한다.

※ 외부 콘텐츠는 저작권을 준수하며, 원문을 복제하지 않고 출처와 링크를 제공하는 방식으로 운영한다.

---

#### Article

저장 항목

- Category
- Title
- Summary
- Thumbnail
- Source
- Source URL
- Published Date
- Created At
- Updated At

---

#### Features

- 카테고리별 조회
- 최신순 정렬
- 검색
- 관리자 게시
- 관리자 수정
- 관리자 삭제

---

#### Validation

- 출처 필수
- URL 형식 검증
- 카테고리 필수

---

#### MVP Scope

포함

- 관리자 게시
- 관리자 수정
- 관리자 삭제
- 카테고리 분류
- 검색

제외

- AI 요약
- 개인 맞춤 추천
- 즐겨찾기

### 3.7 Banner

#### Purpose

앱 메인 화면 상단에 공지, 이벤트, 교육, 프로모션 등의 정보를 노출한다.

---

#### Banner Types

- Notice
- Event
- Promotion
- Information

---

#### Banner Information

저장 항목

- Title
- Description
- Image
- Link URL
- Display Order
- Start Date
- End Date
- Active Status
- Created At
- Updated At

---

#### Features

관리자는 다음 기능을 수행할 수 있다.

- 배너 등록
- 배너 수정
- 배너 삭제
- 노출 순서 변경
- 노출 기간 설정
- 활성화/비활성화

사용자는 다음 기능을 사용할 수 있다.

- 배너 조회
- 배너 클릭
- 외부 링크 이동

---

#### Validation

- 이미지 필수
- 링크 URL 형식 검증
- 노출 기간 검증
- Display Order 중복 방지

---

#### MVP Scope

포함

- 이미지 배너
- 링크 이동
- 관리자 관리
- 노출 순서 관리
- 노출 기간 설정

제외

- 동영상 배너
- 배너 클릭 통계
- A/B 테스트
- 개인 맞춤 배너

### 3.8 Admin

#### Purpose

관리자가 서비스 운영, 콘텐츠 관리 및 사용자 관리를 수행할 수 있도록 한다.

---

#### Dashboard

관리자는 대시보드에서 다음 정보를 확인할 수 있다.

- 총 회원 수
- 오늘 가입한 회원 수
- 활성 사용자 수
- 게시글 수
- 댓글 수
- 다이브 로그 수

---

#### User Management

지원 기능

- 회원 목록 조회
- 회원 검색
- 회원 상세 조회
- 계정 정지
- 계정 정지 해제
- 게시글 작성 제한
- 게시글 작성 제한 해제
- 휴면 계정 조회

---

#### Community Management

지원 기능

- 게시글 삭제
- 댓글 삭제
- 게시글 검색
- 신고 누적 확인(향후)

---

#### Information Management

지원 기능

- 정보글 작성
- 정보글 수정
- 정보글 삭제
- 카테고리 관리

---

#### Banner Management

지원 기능

- 배너 등록
- 배너 수정
- 배너 삭제
- 노출 순서 변경
- 노출 기간 관리

---

#### Permission

관리자 권한은 Role 기반으로 관리한다.

MVP

- Super Admin

향후

- Admin
- Editor
- Moderator

---

#### Audit Log

모든 관리자 작업은 로그를 남긴다.

기록 항목

- 작업자
- 작업 내용
- 대상
- 작업 시간
- IP (향후)

---

#### Validation

- 관리자 권한 검증
- 역할(Role) 검증
- 작업 대상 검증

---

#### MVP Scope

포함

- 회원 관리
- 게시글 관리
- 정보글 관리
- 배너 관리
- 관리자 로그

제외

- 세부 권한(Role) 관리
- 관리자 알림
- 관리자 통계 분석

## 4. Non-Functional Requirements

### Performance

- 앱 실행 시간은 3초 이내를 목표로 한다.
- 일반 API 응답 시간은 500ms 이하를 목표로 한다.
- 이미지 업로드 진행 상태를 사용자에게 표시한다.
- 스크롤은 60 FPS를 유지하는 것을 목표로 한다.

---

### Availability

- Render 장애를 제외하고 서비스는 안정적으로 동작해야 한다.
- 예외 발생 시 앱이 종료되지 않고 적절한 오류 메시지를 제공한다.

---

### Security

- HTTPS 통신만 허용한다.
- JWT 기반 인증을 사용한다.
- Refresh Token을 지원한다.
- API Key는 환경 변수로 관리한다.
- 사용자 개인정보는 최소한으로 저장한다.

---

### Reliability

- 모든 API는 예외 처리를 구현한다.
- 데이터 무결성을 유지한다.
- 이미지 업로드 실패 시 재시도할 수 있다.

---

### Scalability

향후 아래 기능을 쉽게 추가할 수 있어야 한다.

- Apple Login
- Apple Watch
- Apple Health
- O₂ Table
- AI 기능
- Push Notification
- 다국어 지원

---

### Maintainability

- MVVM Architecture
- Repository Pattern
- Service Layer
- SOLID Principles
- 문서와 코드 동기화 유지

---

### Usability

- Apple Human Interface Guidelines(HIG)를 따른다.
- 직관적인 UI를 제공한다.
- 접근성을 고려한다.

---

### Logging

다음 항목은 서버 로그를 남긴다.

- 로그인
- 회원가입
- 관리자 작업
- 오류
- API 예외

---

### Backup

- PostgreSQL 정기 백업
- 이미지 데이터 백업 정책 수립

## 5. Constraints

### Platform

- iOS Only (MVP)
- SwiftUI 기반 개발
- Swift Playground 사용

---

### Backend

- FastAPI
- PostgreSQL (Render)
- REST API

---

### Third-party Services

- Naver Login API
- Google Login API
- Naver Search API
- Naver Map API

---

### Development Rules

- Documentation First
- MVVM Architecture
- Repository Pattern
- Service Layer
- Git 기반 버전 관리

---

## 6. Future Features

### Authentication

- Apple Login
- Local Account
- 2FA

---

### Dive

- Dive Map
- Dive Sharing
- PDF Export
- Import / Export

---

### Training

- O₂ Table
- Static Training
- Dynamic Training
- Apple Health
- Apple Watch

---

### Community

- Like
- Bookmark
- Report
- Reply Comment
- Notification

---

### AI

- AI Dive Analysis
- AI Training Recommendation
- AI Log Summary
- AI Chat Assistant

---

### Service

- Push Notification
- Multi-language
- Dark Mode
- Offline Mode
- Tablet UI

---

## 7. MVP Scope

### Included

- Social Login
- Dive Log
- CO₂ Table
- Certification Management
- Community
- Information
- Banner
- Admin
- My Page

---

### Excluded

- AI Features
- Apple Watch
- Apple Health
- Push Notification
- Offline Mode
- Apple Login
- Local Account
- Dive Map
- Report System

---

### MVP Goal

사용자가 다이빙 활동을 기록하고, 훈련을 수행하며, 커뮤니티를 이용할 수 있는 안정적인 서비스를 제공한다.
