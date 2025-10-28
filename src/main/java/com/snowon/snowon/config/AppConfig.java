package com.snowon.snowon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // 이 클래스는 '설정' 파일임을 선언
public class AppConfig {

    @Bean // 이 메서드가 반환하는 객체(RestTemplate)를 Spring Bean으로 등록
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}