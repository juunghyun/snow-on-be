package com.snowon.snowon.scheduler;

import com.snowon.snowon.service.WeatherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;

    @PostConstruct
    public void initWeatherCache(){
        log.info("애플리케이션 시작 - 초기 날씨 정보 1회 적재");
        weatherService.updateAllCityWeather();
    }

    /**
     * 매시 10분에 날씨 정보를 갱신합니다.
     * (공공 API가 매시 00분에 데이터를 생성하므로 -> 10분이 가장 안정적)
     *
     * Cron 표현식: "초 분 시 일 월 요일"
     * "0 50 * * * *" = 매시 50분 0초에 실행
     */
    //@Scheduled(initialDelay = 1000, fixedDelay = 30000) //테스트용
    @Scheduled(cron = "0 10 * * * *")
    public void scheduleWeatherUpdate() {
        log.info("정기 날씨 정보 갱신 스케줄러 실행...");
        weatherService.updateAllCityWeather();
    }
}