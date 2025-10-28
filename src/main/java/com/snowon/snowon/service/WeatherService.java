package com.snowon.snowon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowon.snowon.client.WeatherApiClient;
import com.snowon.snowon.domain.City;
import com.snowon.snowon.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j // 로그를 찍기 위해 사용 (Lombok)
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (Lombok)
public class WeatherService {

    private final CityRepository cityRepository;
    private final WeatherApiClient weatherApiClient;
    private final StringRedisTemplate redisTemplate; // Redis와 통신 (Key, Value가 모두 String일 때 사용)
    private final ObjectMapper objectMapper;     // JSON 문자열을 파싱 (spring-web이 기본 포함)

    /**
     * DB의 모든 도시 날씨 정보를 갱신하여 Redis에 저장합니다.
     */
    public void updateAllCityWeather() {
        log.info("날씨 정보 갱신을 시작합니다...");

        // 1. DB에서 23개 도시 목록을 가져옴 - 쿼리 1회
        List<City> cities = cityRepository.findAll();

        // 2. 23개 도시를 순회 (N번 루프)
        for (City city : cities) {
            try {
                // 3. API 클라이언트를 호출 (N번의 동기/블로킹 API 호출 발생)
                String jsonResponse = weatherApiClient.getUltraShortTermWeather(city.getNx(), city.getNy());

                // 4. JSON 파싱해서 "PTY"(강수형태) 값 추출
                String ptyValue = parsePtyValue(jsonResponse);

                // 5. Redis에 저장 (Key: "weather::1", Value: "3")
                //    '1시간 + 5분' 동안 유효하게 저장 (스케줄러 주기보다 약간 길게)
                String redisKey = "weather::" + city.getId();
                redisTemplate.opsForValue().set(redisKey, ptyValue, 65, TimeUnit.MINUTES);

                log.info("[{}] 날씨 갱신 완료 (PTY: {})", city.getName(), ptyValue);

            } catch (Exception e) {
                // 한 도시가 실패해도, 전체 스케줄러가 멈추면 안 되므로 try-catch로 감쌉니다.
                log.error("[{}] 날씨 갱신 중 에러 발생: {}", city.getName(), e.getMessage());
            }
        }
        log.info("날씨 정보 갱신을 완료했습니다.");
    }

    /**
     * API 응답(JSON 문자열)을 파싱하여 PTY(강수형태) 값을 반환합니다.
     */
    private String parsePtyValue(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            for (JsonNode item : items) {
                if ("PTY".equals(item.path("category").asText())) {
                    return item.path("obsrValue").asText();
                }
            }
            return "0"; // PTY 항목이 없는 경우 '맑음(0)'으로 간주
        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            return "0"; // 파싱 실패 시 '맑음(0)'으로 간주
        }
    }
}