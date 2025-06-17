package com.nsmm.esg.gateway_service.util;

import com.nsmm.esg.gateway_service.dto.JwtClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JWT 토큰 검증 및 파싱 유틸리티
 * Auth Service와 동일한 Secret Key 사용
 */
@Slf4j
@Component
public class JwtUtil {

  private final SecretKey secretKey;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
  }

  /**
   * 토큰 유효성 검증
   */
  public boolean validateToken(String token) {
    try {
      getClaimsFromToken(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.debug("잘못된 JWT 서명: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.debug("만료된 JWT 토큰: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.debug("지원되지 않는 JWT 토큰: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.debug("JWT 토큰이 잘못됨: {}", e.getMessage());
    } catch (Exception e) {
      log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
    }
    return false;
  }

  /**
   * 토큰에서 모든 클레임 정보 추출
   */
  public JwtClaims getAllClaimsFromToken(String token) {
    Claims claims = getClaimsFromToken(token);

    return JwtClaims.builder()
        .accountNumber(claims.getSubject())
        .companyName(claims.get("companyName", String.class))
        .userType(claims.get("userType", String.class))
        .level(claims.get("level", Integer.class))
        .treePath(claims.get("treePath", String.class))
        .headquartersId(claims.get("headquartersId", Long.class))
        .userId(claims.get("userId", Long.class))
        .build();
  }

  /**
   * 토큰에서 Claims 추출
   */
  private Claims getClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}