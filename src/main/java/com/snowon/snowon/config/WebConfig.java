package com.snowon.snowon.config; // 패키지 경로는 맞게 수정하세요

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 이 클래스가 설정 파일임을 선언
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // '/api/'로 시작하는 모든 경로에 적용
                .allowedOrigins("http://localhost:3000") // 허용할 출처 (프론트엔드 주소)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(false); // 쿠키/인증 정보 허용 여부 (필요 없으므로 false)
        // .maxAge(3600); // Pre-flight 요청 캐시 시간 (초)
    }
}