package com.snowon.snowon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application-prod.yml의 cors.allowed-origin 값을 주입
    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 환경 변수로 받은 주소(들)을 콤마(,)로 분리 -> 배열로 만들기.
        // Docker Compose에서 하나의 문자열로 주입받기 때문에 필요.
        String[] origins = allowedOrigin.split(",");

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}