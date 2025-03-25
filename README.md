<<<<<<< HEAD
# reservation_api_project
매장 등록, 예약, 회원 관리 및 조회 기능을 구현한 Spring boot기반 프로젝트
=======
<p align="center">
  <img src="https://github.com/DoHyunDaniel/reservation_api_project/blob/main/logo.png?raw=true" width="200" alt="Reservation Service Logo" />
</p>
# 📌 Reservation Service - 예약 관리 시스템

> 매장 예약, 리뷰, 점주 관리, 사용자 인증을 포함한 통합 예약 플랫폼  
> Spring Boot 기반의 백엔드 애플리케이션입니다.

---

## ✅ 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 회원가입 / 로그인 | JWT 기반 인증, 비밀번호 암호화 (BCrypt) |
| 🧑 사용자 관리 | 회원 탈퇴, 비밀번호/닉네임/유형 수정 |
| 🏪 점주 매장 관리 | 점주 전용 매장 등록 / 수정 / 삭제 |
| 📅 예약 관리 | 사용자 예약 생성 / 취소 / 삭제 / 점주 승인 |
| 📝 리뷰 관리 | 예약 기반 리뷰 작성 / 수정 / 이미지 업로드 |
| ☁️ 이미지 업로드 | AWS S3 연동 이미지 저장 및 삭제 |
| 🧭 매장 정렬 기능 | 평점순 / 거리순 / 이름순 정렬 가능 |

---

## ⚙️ 기술 스택

| 분야 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build Tool | Gradle |
| DB | MySQL |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT |
| Cloud | AWS S3 |
| Validation | Jakarta Bean Validation |
| Testing | JUnit5 + Mockito + AssertJ |
| 기타 | Thymeleaf (템플릿), Lombok, H2 (테스트용) |

---

## 🛡️ 보안 설계

- **JWT 인증**  
  - 로그인 시 사용자 ID, ROLE 포함된 토큰 발급  
  - 매 요청 시 `Authorization: Bearer {token}` 헤더를 통해 인증
- **Spring Security 기반 권한 설정**  
  - `USER`, `OWNER`, `ADMIN` 역할별 접근 제어
  - 각 엔드포인트에 권한별 인가 정책 적용
- **비밀번호 암호화**  
  - `BCryptPasswordEncoder` 사용
- **예약/리뷰/이미지 접근 제한**  
  - 사용자 본인 확인 및 소유자 검증 철저

---

## 📁 API 예시

```bash
POST /users/signup
POST /users/auth/login
GET  /stores/list?sortBy=rating
POST /reservation/reserve
POST /reviews/create (multipart/form-data)
PUT  /reservation/confirm
```

---

## 🧪 테스트

- 단위 테스트: `@MockBean`, `Mockito`, `AssertJ` 사용
- 서비스 계층 단위 테스트 완비 (`UserService`, `StoreService`, `ReservationService`, `ReviewService` 등)

---

## 🚀 실행 방법

1. `.env` 또는 `application.yml`에 다음 환경 변수 설정

```yaml
jwt:
  secret: YOUR_BASE64_SECRET_KEY
  experation: 3600000

cloud:
  aws:
    credentials:
      access-key: YOUR_AWS_ACCESS_KEY
      secret-key: YOUR_AWS_SECRET_KEY
    region:
      static: ap-northeast-2
    s3:
      bucket: your-s3-bucket-name
```

2. 프로젝트 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun
```

3. Postman 또는 Swagger에서 API 테스트

---

## 🖼️ ERD / 아키텍처 (선택)

> 원하는 경우 ERD 이미지나 구조도 삽입 가능

---

## 🙌 기여 및 확장 아이디어

- 관리자 페이지 분리 및 통계 대시보드 구현
- 예약 알림 (Email / FCM) 연동
- 프론트엔드 (React, Vue 등)와 통합
- Redis 캐싱 및 트래픽 최적화

---

## 📄 라이선스

본 프로젝트는 MIT 라이선스를 따릅니다.

---

## ✨ Special Thanks

이 프로젝트는 학습과 실무 준비를 위해 기획되었으며,  
Spring Boot 기반의 인증, 인가, 클린 아키텍처 설계를 경험하는 데 중점을 두었습니다. 🙏
>>>>>>> branch 'main' of https://github.com/DoHyunDaniel/reservation_api_project.git
