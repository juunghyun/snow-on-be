package com.snowon.snowon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration // 이 클래스는 '설정' 파일임을 선언
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 2. 기본 RestTemplate 대신, 타임아웃 설정을 위한 Factory 사용
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 3. 타임아웃 설정 (예: 5초)
        // 연결 타임아웃: 서버와 연결을 맺는 데 걸리는 최대 시간
        factory.setConnectTimeout(Duration.ofSeconds(5)); // Java 8 Duration 사용 권장
        // 읽기 타임아웃: 연결 후 데이터를 읽어오는 데 걸리는 최대 시간
        factory.setReadTimeout(Duration.ofSeconds(5));    // Java 8 Duration 사용 권장
        // (참고: 구버전에서는 factory.setConnectTimeout(5000); // ms 단위 int 사용)

        return new RestTemplate(factory); // 4. 타임아웃이 설정된 Factory로 RestTemplate 생성
    }
}