package com.snowon.snowon.service;

import com.fasterxml.jackson.core.type.TypeReference; // [추가] JSON List<T> 파싱용
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowon.snowon.client.WeatherApiClient;
import com.snowon.snowon.domain.City;
import com.snowon.snowon.domain.WeatherStatus;
import com.snowon.snowon.dto.MapDataResponse;
import com.snowon.snowon.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // [추가] 트랜잭션용

import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String CACHE_KEY_MAP_DATA = "weather:map:data";
    private final CityRepository cityRepository;
    private final WeatherApiClient weatherApiClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * [수정] DB의 모든 도시 날씨 정보를 갱신하여 'DB'에 저장하고 '메인 캐시'를 삭제합니다.
     * (이 메서드는 스케줄러가 1시간 주기로 호출합니다)
     */
    @Transactional
    public void updateAllCityWeather() {
        log.info("날씨 정보 갱신을 시작합니다...");
        List<City> cities = cityRepository.findAll(); // 1. DB에서 23개 도시 목록을 가져옴

        for (City city : cities) {
            String ptyValue = "0"; // 기본값
            try {

                String jsonResponse = weatherApiClient.getUltraShortTermWeather(city.getNx(), city.getNy());
                ptyValue = parsePtyValue(jsonResponse); // 3. JSON 파싱

                log.info("[{}] 날씨 갱신 완료 (PTY: {})", city.getName(), ptyValue);

            } catch (Exception e) {
                // 3번 재시도 후에도 최종 실패 시
                log.error("[{}] 날씨 갱신 3회 재시도 모두 실패: {}", city.getName(), e.getMessage());
                log.warn("[{}] 날씨 정보를 기본값(맑음)으로 저장합니다.", city.getName());

            } finally {

                city.setPtyCode(ptyValue);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.warn("날씨 갱신 중 스레드 대기 인터럽트 발생");
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("DB 갱신 완료. Redis 메인 캐시를 삭제합니다.");
        redisTemplate.delete(CACHE_KEY_MAP_DATA);
        log.info("날씨 정보 갱신을 완료했습니다.");
    }

    /**
     *
     * @return 23개 도시의 날씨 정보 리스트
     */
    public List<MapDataResponse> getMapWeatherData() {
        try {

            String cachedData = redisTemplate.opsForValue().get(CACHE_KEY_MAP_DATA);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData,
                        new TypeReference<List<MapDataResponse>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 읽기/파싱 실패: {}", e.getMessage());
        }

        log.info("Cache Miss: DB에서 Map 데이터를 조회하고 캐시를 채웁니다.");


        List<City> cities = cityRepository.findAll();
        List<MapDataResponse> mapDataList = cities.stream()
                .map(city -> {
                    String ptyCode = (city.getPtyCode() != null) ? city.getPtyCode() : "0";
                    WeatherStatus status = WeatherStatus.fromCode(ptyCode);
                    return new MapDataResponse(city, status, ptyCode);
                })
                .toList();

        try {
            String jsonData = objectMapper.writeValueAsString(mapDataList);
            redisTemplate.opsForValue().set(CACHE_KEY_MAP_DATA, jsonData, 3540, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 캐시 쓰기 실패: {}", e.getMessage());
        }

        return mapDataList;
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