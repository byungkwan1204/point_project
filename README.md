# 무료 포인트 시스템

적립, 적립 취소, 사용, 사용 취소를 처리하는 포인트 시스템 과제입니다.

---

## 💡 프로젝트 설명

본 프로젝트는 사용자별로 **포인트**를 관리하는 시스템입니다.

### 기술 스택
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (테스트, 로컬 환경)
- JUnit5
- Gradle

### 지원 기능
- 포인트 적립
- 포인트 적립 취소
- 포인트 사용
- 포인트 사용 취소
- 포인트 적립 및 사용 이력 관리

### 설계 특징
- **DDD, TDD 기반 개발**  
  도메인 레이어부터 서비스까지 테스트 주도 개발 적용
- **Event 기반 이력 관리**  
  `ApplicationEventPublisher`를 사용해 포인트 적립/사용/취소 시 `PointHistory` 자동 기록.
  
  `Outbox 패턴`을 사용해 이벤트 유실 방지
- **정책 기반 유효성 검사**  
  최대 무료 포인트 한도는 `application.yml` 기반으로 주입
- `비관적 락(Pessimistic Lock)`을 사용하여 동시성 처리

---

## ⚙️ 빌드 및 실행 방법

1️⃣ Java 21, Gradle 8.x 환경에서 진행
```bash
./gradlew clean build
````
2️⃣ 로컬 실행
```bash
./gradlew bootRun
```
3️⃣ 테스트 실행
```bash
./gradlew test
````
