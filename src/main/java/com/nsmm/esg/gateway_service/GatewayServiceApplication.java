package com.nsmm.esg.gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ESG Gateway Service 메인 클래스
 * 
 * 주요 기능:
 * - 마이크로서비스 라우팅
 * - JWT 쿠키 기반 인증 처리
 * - 사용자 정보 헤더 변환
 * - 글로벌 CORS 설정
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
