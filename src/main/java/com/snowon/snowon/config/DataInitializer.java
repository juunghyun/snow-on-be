package com.snowon.snowon.config;

import com.snowon.snowon.service.CitySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CitySyncService citySyncService;

    // CommandLineRunner는 Spring Boot 앱이 시작될 때
    // 'run' 메서드를 "딱 1번" 자동으로 실행합니다.
    @Override
    public void run(String... args) throws Exception {
        log.info("데이터 초기화 작업을 시작합니다...");

        // MySQL -> ES 동기화 실행
        citySyncService.syncCitiesToElasticsearch();

        log.info("데이터 초기화 작업을 완료했습니다.");
    }
}