package com.snowon.snowon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. application-prod.yml에서 정의한 cors.allowed-origin 값을 주입받습니다.
    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 2. 환경 변수로 받은 주소(들)을 콤마(,)로 분리하여 배열로 만듭니다.
        //    (Docker Compose에서 하나의 문자열로 주입받기 때문에 필요합니다.)
        String[] origins = allowedOrigin.split(",");

        registry.addMapping("/api/**") // '/api/'로 시작하는 모든 경로에 적용
                .allowedOrigins(origins) // 3. 환경 변수에서 받은 승인된 주소만 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}