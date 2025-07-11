package com.nsmm.esg.gateway_service.filter;

import com.nsmm.esg.gateway_service.dto.JwtClaims;
import com.nsmm.esg.gateway_service.util.JwtUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 인증 Gateway Filter
 * 쿠키에서 JWT 추출 → 검증 → 사용자 정보 헤더로 변환
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

  private final JwtUtil jwtUtil;

  public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
    super(Config.class);
    this.jwtUtil = jwtUtil;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getURI().getPath();

      log.debug("=== Gateway Filter 시작 ===");
      log.debug("요청 경로: {}", path);

      // 공개 API는 JWT 검증 제외
      if (isExcludedPath(path, config.getExcludePaths())) {
        log.debug("공개 API 경로, JWT 검증 제외: {}", path);
        return chain.filter(exchange);
      }

      // 쿠키에서 JWT 추출
      String jwt = extractJwtFromCookie(request);
      log.debug("추출된 JWT 토큰: {}", jwt != null ? "존재" : "없음");

      if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
        try {
          // JWT 디코딩
          JwtClaims claims = jwtUtil.getAllClaimsFromToken(jwt);

          log.debug("=== JWT 클레임 정보 ===");
          log.debug("계정 번호: {}", claims.getAccountNumber());
          log.debug("사용자 타입: {}", claims.getUserType());
          log.debug("회사명: {}", claims.getCompanyName());
          log.debug("본사 ID: {}", claims.getHeadquartersId());
          log.debug("파트너 ID: {}", claims.getPartnerId());
          log.debug("레벨: {}", claims.getLevel());
          log.debug("트리 경로: {}", claims.getTreePath());

          // 기존 사용자 관련 헤더 제거 (보안 - 클라이언트 조작 방지)
          ServerHttpRequest modifiedRequest = request.mutate()
              .headers(headers -> {
                headers.remove("X-USER-TYPE");
                headers.remove("X-HEADQUARTERS-ID");
                headers.remove("X-PARTNER-ID");
                headers.remove("X-ACCOUNT-NUMBER");
                headers.remove("X-COMPANY-NAME");
                headers.remove("X-LEVEL");
                headers.remove("X-TREE-PATH");
              })
              .build();

          // 본사와 파트너사 구분하여 처리
          if ("HEADQUARTERS".equals(claims.getUserType())) {
            modifiedRequest = modifiedRequest.mutate()
                .header("X-USER-TYPE", claims.getUserType())
                .header("X-COMPANY-NAME", claims.getCompanyName())
                .header("X-ACCOUNT-NUMBER", claims.getAccountNumber())
                .header("X-HEADQUARTERS-ID", String.valueOf(claims.getHeadquartersId()))
                .header("X-TREE-PATH", "/" + claims.getAccountNumber() + "/")
                .header("X-LEVEL", "0")
                .build();
          } else if ("PARTNER".equals(claims.getUserType())) {
            modifiedRequest = modifiedRequest.mutate()
                .header("X-USER-TYPE", claims.getUserType())
                .header("X-COMPANY-NAME", claims.getCompanyName())
                .header("X-ACCOUNT-NUMBER", claims.getAccountNumber())
                .header("X-HEADQUARTERS-ID", String.valueOf(claims.getHeadquartersId()))
                .header("X-PARTNER-ID", String.valueOf(claims.getPartnerId()))
                .header("X-TREE-PATH", claims.getTreePath())
                .header("X-LEVEL", String.valueOf(claims.getLevel()))
                .build();
          }

          log.debug("=== 추가된 헤더 정보 ===");
          modifiedRequest.getHeaders().forEach((key, value) -> {
            log.debug("{}: {}", key, value);
          });

          log.debug("JWT 인증 성공 및 헤더 추가 완료");
          return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
          log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
          log.error("상세 에러: ", e);
          return handleUnauthorized(exchange);
        }
      }

      // 인증 실패
      log.warn("JWT 인증 실패 - 토큰 없음 또는 유효하지 않음: {}", path);
      return handleUnauthorized(exchange);
    };
  }

  /**
   * 쿠키에서 JWT 토큰 추출
   */
  private String extractJwtFromCookie(ServerHttpRequest request) {
    if (request.getCookies().containsKey("jwt")) {
      return request.getCookies().getFirst("jwt").getValue();
    }
    return null;
  }

  /**
   * 제외 경로 확인
   */
  private boolean isExcludedPath(String path, String excludePaths) {
    if (!StringUtils.hasText(excludePaths)) {
      return false;
    }

    List<String> excludeList = Arrays.asList(excludePaths.split(","));
    return excludeList.stream()
        .map(String::trim)
        .anyMatch(path::startsWith);
  }

  /**
   * 인증 실패 처리
   */
  private Mono<Void> handleUnauthorized(org.springframework.web.server.ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  /**
   * Filter 설정 클래스
   */
  @Data
  public static class Config {
    private String excludePaths;
  }
}