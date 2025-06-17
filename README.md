# ESG Gateway Service

> **API Gateway** - Spring Cloud Gateway 기반 중앙집중식 라우팅 및 보안 처리

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Private-red.svg)]()

## 🎯 프로젝트 개요

ESG 프로젝트의 **핵심 인프라 컴포넌트**로, 모든 클라이언트 요청의 **단일 진입점(Single Entry Point)** 역할을 수행합니다. **반응형 프로그래밍**과 **커스텀 필터 체인**을 통해 엔터프라이즈급 보안 및 라우팅 기능을 제공합니다.

### 🔥 핵심 기술적 특징

- **🚀 반응형 아키텍처**: Spring WebFlux 기반 비동기/논블로킹 처리
- **🛡️ 중앙집중식 보안**: JWT 토큰 검증 및 헤더 변환
- **⚡ 동적 라우팅**: Eureka 기반 서비스 디스커버리 연동
- **🔒 보안 강화**: 클라이언트 헤더 조작 방지 및 토큰 검증
- **📊 실시간 모니터링**: Spring Boot Actuator 통합

## 🏗️ 시스템 아키텍처

### API Gateway 중심 마이크로서비스 구조

본 Gateway Service는 **마이크로서비스 아키텍처의 핵심 허브**로서 다음과 같은 역할을 수행합니다:

- **단일 진입점**: 모든 클라이언트 요청의 중앙집중식 처리
- **보안 게이트웨이**: JWT 인증 및 권한 검증
- **라우팅 허브**: 동적 서비스 발견 및 로드 밸런싱
- **CORS 관리**: 통합 CORS 정책 적용

### JWT 인증 및 요청 처리 플로우

JWT 토큰 기반의 **보안 중심 설계**로 다음과 같은 프로세스를 구현했습니다:

1. **토큰 추출**: HttpOnly 쿠키에서 JWT 토큰 안전 추출
2. **토큰 검증**: 서명, 만료시간, 구조 검증
3. **헤더 변환**: JWT Claims를 마이크로서비스용 헤더로 변환
4. **보안 강화**: 클라이언트 헤더 조작 방지
5. **라우팅**: 검증된 요청을 적절한 서비스로 전달

## 🛠️ 기술 스택

### 핵심 프레임워크

- **Spring Boot 3.5.0** - 최신 Spring 생태계
- **Spring Cloud Gateway** - 반응형 API Gateway
- **Spring Cloud Netflix Eureka** - 서비스 디스커버리 클라이언트
- **Spring WebFlux** - 비동기/논블로킹 웹 프레임워크

### 보안 & 인증

- **JJWT 0.11.5** - JWT 토큰 처리
- **Custom Filter Chain** - 보안 필터 구현
- **HttpOnly Cookie** - XSS 방지 토큰 저장

### 모니터링 & 운영

- **Spring Boot Actuator** - 애플리케이션 메트릭
- **Eureka Health Check** - 서비스 상태 모니터링
- **Logback** - 구조화된 로깅

## ⚡ 주요 기능

### 1. **고성능 라우팅 시스템**

```yaml
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

### 2. **커스텀 JWT 인증 필터**

**JwtAuthenticationGatewayFilterFactory** 클래스를 직접 구현하여 다음 기능을 제공:

```java
@Component
public class JwtAuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<Config> {

    // 핵심 기능:
    // 1. 쿠키에서 JWT 추출
    // 2. 토큰 검증 (서명, 만료시간)
    // 3. Claims 디코딩 및 헤더 변환
    // 4. 클라이언트 헤더 조작 방지
    // 5. 공개 API 자동 필터링
}
```

### 3. **보안 헤더 변환**

JWT Claims를 마이크로서비스에서 활용 가능한 HTTP 헤더로 변환:

| JWT Claims       | 변환된 헤더         | 설명                               |
| ---------------- | ------------------- | ---------------------------------- |
| `accountNumber`  | `X-Account-Number`  | 계정 번호 (2412161700-L1-001)      |
| `userType`       | `X-User-Type`       | 사용자 타입 (HEADQUARTERS/PARTNER) |
| `companyName`    | `X-Company-Name`    | 회사명 (삼성전자)                  |
| `treePath`       | `X-Tree-Path`       | 계층 구조 경로 (/1/L1-001/)        |
| `userId`         | `X-User-Id`         | 사용자 고유 ID                     |
| `headquartersId` | `X-Headquarters-Id` | 본사 ID                            |
| `level`          | `X-Level`           | 협력사 레벨 (1, 2, 3...)           |

### 4. **동적 서비스 발견**

- **Eureka Client**: 마이크로서비스 자동 발견
- **Load Balancing**: Netflix Ribbon 통합
- **Health Check**: 실시간 서비스 상태 모니터링
- **Failover**: 서비스 장애 시 자동 라우팅

## 🔒 보안 설계

### 다층 보안 아키텍처

#### 1. **클라이언트 헤더 조작 방지**

```java
// 모든 사용자 관련 헤더를 제거하여 조작 방지
ServerHttpRequest modifiedRequest = request.mutate()
    .headers(headers -> {
        headers.remove("X-User-Id");
        headers.remove("X-User-Type");
        headers.remove("X-Headquarters-Id");
        // ... 모든 인증 관련 헤더 제거
    })
    // Gateway에서만 신뢰할 수 있는 헤더 추가
    .header("X-User-Id", String.valueOf(claims.getUserId()))
    .header("X-User-Type", claims.getUserType())
    // ...
    .build();
```

#### 2. **JWT 토큰 3단계 검증**

- **서명 검증**: HMAC SHA-256 알고리즘
- **만료 시간 검증**: 토큰 유효기간 확인
- **구조 검증**: JWT 표준 형식 준수 확인

#### 3. **공개 API 화이트리스트**

```java
// 인증이 필요 없는 API 경로 설정
private boolean isExcludedPath(String path, String excludePaths) {
    // 로그인, 회원가입 등 JWT 검증 제외
    List<String> excludeList = Arrays.asList(excludePaths.split(","));
    return excludeList.stream().anyMatch(path::startsWith);
}
```

### 토큰 보안 정책

- **저장 방식**: HttpOnly Cookie (XSS 공격 방지)
- **전송 보안**: Secure, SameSite=Strict 설정
- **만료 관리**: Access Token 15분, Refresh Token 7일
- **암호화**: 쿠키 값 자체가 JWT (별도 암호화 불필요)

## 📊 모니터링 & 운영

### Spring Boot Actuator 통합

| 엔드포인트                 | 기능        | 활용 방안                 |
| -------------------------- | ----------- | ------------------------- |
| `/actuator/health`         | 헬스 체크   | 로드밸런서 상태 확인      |
| `/actuator/gateway/routes` | 라우팅 정보 | 등록된 라우트 실시간 조회 |
| `/actuator/metrics`        | 성능 메트릭 | 처리량, 응답시간 모니터링 |
| `/actuator/info`           | 서비스 정보 | 버전, 빌드 정보 추적      |

### 로깅 전략

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG # Gateway 라우팅 로그
    com.nsmm.esg.gateway_service: DEBUG # 커스텀 필터 로그
    org.springframework.security: WARN # 보안 관련 경고
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## 🚀 성능 최적화

### 반응형 프로그래밍 활용

**Spring WebFlux** 기반으로 다음과 같은 성능 이점을 구현:

- **Non-blocking I/O**: 높은 동시성 처리 (수천 개 동시 연결)
- **Event-driven**: 이벤트 기반 비동기 요청 처리
- **Backpressure**: 부하 제어를 통한 시스템 안정성
- **Resource Efficiency**: 적은 메모리로 더 많은 요청 처리

### 캐싱 전략

- **Eureka 서비스 캐시**: 30초 주기 서비스 목록 갱신
- **Route 캐시**: 메모리 기반 라우팅 규칙 캐싱
- **JWT 검증 캐시**: 동일 토큰 중복 검증 방지

## 🔧 로컬 개발 환경

### 실행 순서 (의존성 고려)

```bash
# 1. 서비스 디스커버리 먼저 실행
cd backend/discovery-service && ./gradlew bootRun

# 2. 설정 서버 실행 (선택사항)
cd backend/config-service && ./gradlew bootRun

# 3. 인증 서비스 실행
cd backend/auth-service && ./gradlew bootRun

# 4. Gateway 서비스 실행 (마지막)
cd backend/gateway-service && ./gradlew bootRun
```

### 필수 환경 변수

```bash
# JWT 시크릿 (Auth Service와 동일해야 함)
export JWT_SECRET="your-256-bit-secret-key"

# Eureka 서버 주소
export EUREKA_SERVER_URL="http://localhost:8761/eureka"

# 선택적 설정
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:8080"
```

### 서비스 상태 확인

```bash
# Gateway 서비스 상태
curl http://localhost:8080/actuator/health

# 등록된 라우트 확인
curl http://localhost:8080/actuator/gateway/routes | jq .

# Eureka 대시보드에서 서비스 등록 확인
open http://localhost:8761
```

## 📈 확장성 고려사항

### 수평 확장 (Horizontal Scaling)

```yaml
# 다중 Gateway 인스턴스 실행 지원
server:
  port: ${PORT:8080} # 환경 변수로 동적 포트 할당

eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.cloud.client.hostname}:${random.int}
    prefer-ip-address: true
```

### 향후 확장 계획

1. **Rate Limiting**: Redis 기반 속도 제한 구현
2. **Circuit Breaker**: Resilience4j 통합으로 장애 격리
3. **API 버저닝**: 경로 기반 버전 관리 시스템
4. **분산 캐싱**: Redis Cluster 기반 캐시 레이어
5. **메트릭 수집**: Prometheus + Grafana 연동

## 🎯 구현 하이라이트

### 아키텍처 설계 역량

- **마이크로서비스 패턴**: Single Entry Point 구현
- **API Gateway 패턴**: 중앙집중식 요청 관리
- **보안 아키텍처**: 다층 보안 시스템 설계

### 기술적 전문성

- **Spring Cloud Gateway**: 커스텀 필터 팩토리 구현
- **반응형 프로그래밍**: WebFlux 기반 고성능 처리
- **JWT 보안**: 토큰 기반 무상태 인증 시스템

### 운영 고려사항

- **모니터링**: Actuator 기반 실시간 상태 추적
- **로깅**: 구조화된 로그 및 디버깅 지원
- **확장성**: 수평 확장 가능한 아키텍처

## 🔗 관련 서비스

- **[Auth Service](../auth-service/README.md)**: JWT 토큰 발급 및 사용자 인증
- **[Discovery Service](../discovery-service/README.md)**: 서비스 등록 및 발견
- **[Config Service](../config-service/README.md)**: 중앙집중식 설정 관리

---

_ESG 프로젝트의 핵심 진입점으로서 안정적이고 확장 가능한 API Gateway를 구현했습니다. 엔터프라이즈급 보안과 성능을 동시에 달성한 아키텍처입니다._
