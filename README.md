# ESG Gateway Service

ESG 프로젝트의 API Gateway 서비스입니다. 모든 클라이언트 요청의 중앙 진입점 역할을 합니다.

## 🎯 주요 기능

### 1. **중앙 인증 처리**

- 쿠키에서 JWT 토큰 추출 및 검증
- 사용자 정보를 헤더로 변환하여 각 마이크로서비스에 전달
- 공개 API 경로 자동 필터링

### 2. **마이크로서비스 라우팅**

- Auth Service: `/api/v1/headquarters/**`, `/api/v1/partners/**`
- Company Service: `/api/v1/companies/**` (미래)
- ESG Data Service: `/api/v1/esg-data/**` (미래)

### 3. **글로벌 CORS 설정**

- 모든 서비스에 대한 통합 CORS 처리
- 개발/운영 환경별 Origin 설정

## 🔧 인증 플로우

```
클라이언트 (브라우저)
    ↓ (쿠키: jwt)
Gateway Service
    ↓ (헤더: X-User-Id, X-User-Type, etc.)
각 마이크로서비스
```

### **헤더 변환 예시**

**입력 (JWT 쿠키):**

```json
{
  "sub": "2412161700-L1-001",
  "companyName": "삼성전자",
  "userType": "PARTNER",
  "level": 1,
  "treePath": "/1/L1-001/",
  "headquartersId": 1,
  "userId": 5
}
```

**출력 (HTTP 헤더):**

```
X-User-Id: 5
X-User-Type: PARTNER
X-Headquarters-Id: 1
X-Account-Number: 2412161700-L1-001
X-Company-Name: 삼성전자
X-Level: 1
X-Tree-Path: /1/L1-001/
```

## 🛡️ 보안 기능

### **1. 클라이언트 헤더 조작 방지**

- 클라이언트가 보낸 사용자 관련 헤더 모두 제거
- Gateway에서만 신뢰할 수 있는 헤더 생성

### **2. JWT 검증**

- Auth Service와 동일한 Secret Key 사용
- 토큰 만료, 서명 검증
- 유효하지 않은 토큰 자동 차단

### **3. 공개 API 필터링**

- 로그인, 회원가입 등 공개 API는 JWT 검증 제외
- 설정 파일에서 제외 경로 관리

## 🚀 실행 방법

### **1. 사전 요구사항**

- Discovery Service (Eureka) 실행 필요
- Auth Service와 JWT Secret 동일해야 함

### **2. 실행 명령어**

```bash
cd backend/gateway-service
./gradlew bootRun
```

### **3. 서비스 확인**

- **Gateway**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Gateway Routes**: http://localhost:8080/actuator/gateway/routes

## ⚙️ 설정

### **application.yml 주요 설정**

```yaml
# 포트 설정
server:
  port: 8080

# JWT 설정 (Auth Service와 동일해야 함)
jwt:
  secret: "mySecretKey..."
  expiration: 900000

# 라우팅 설정
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/headquarters/**, /api/v1/partners/**
          filters:
            - name: JwtAuthenticationGatewayFilter
```

## 🔍 모니터링

### **Actuator 엔드포인트**

- `/actuator/health`: 서비스 상태
- `/actuator/info`: 서비스 정보
- `/actuator/gateway/routes`: 라우팅 정보

### **로깅**

- Gateway 라우팅: DEBUG 레벨
- JWT 처리: DEBUG 레벨
- 인증 실패: WARN 레벨

## 🎮 사용 예시

### **클라이언트에서 요청**

```javascript
// 브라우저에서 로그인 후 API 호출
fetch("http://localhost:8080/api/v1/partners/first-level", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include", // 쿠키 자동 포함
  body: JSON.stringify({
    companyName: "삼성디스플레이",
    email: "display@samsung.com",
    contactPerson: "김협력",
    phone: "02-1111-2222",
  }),
});
```

### **마이크로서비스에서 받는 요청**

```java
@PostMapping("/first-level")
public ResponseEntity<...> createFirstLevelPartner(
    HttpServletRequest request,
    @Valid @RequestBody PartnerCreateRequest requestBody) {

    // Gateway에서 전달한 헤더 사용
    String userId = request.getHeader("X-User-Id");
    String userType = request.getHeader("X-User-Type");
    String headquartersId = request.getHeader("X-Headquarters-Id");

    // JWT 검증 없이 헤더 정보만 신뢰하여 사용
    // ...
}
```

## 🔄 향후 확장

### **새로운 서비스 추가 시**

1. `application.yml`에 라우팅 규칙 추가
2. 필요시 새로운 Filter 개발
3. 모니터링 설정 추가

### **인증 로직 변경 시**

1. `JwtUtil` 클래스 수정
2. `JwtClaims` DTO 구조 변경
3. Filter에서 헤더 매핑 수정

이제 Gateway Service가 완전히 구축되었습니다! 🎉
