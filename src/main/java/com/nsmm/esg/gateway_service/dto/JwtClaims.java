package com.nsmm.esg.gateway_service.dto;

import lombok.*;

/**
 * JWT 클레임 정보 DTO
 * Auth Service의 JwtClaims와 동일한 구조
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {

  private String accountNumber; // 계정번호 (본사: 2412161700, 협력사: 2412161700-L1-001)
  private String companyName; // 회사명
  private String userType; // "HEADQUARTERS" 또는 "PARTNER"
  private Integer level; // 협력사인 경우 레벨 정보 (1, 2, 3...)
  private String treePath; // 협력사인 경우 트리 경로 (/1/L1-001/)
  private Long headquartersId; // 본사 ID (항상 존재)
  private Long partnerId; // 협력사인 경우에만 존재, 본사인 경우 null
}