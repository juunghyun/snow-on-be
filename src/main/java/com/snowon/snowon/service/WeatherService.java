package com.snowon.snowon.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j // 로그를 찍기 위해 사용 (Lombok)
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (Lombok)
public class WeatherService {

    private final CityRepository cityRepository;
    private final WeatherApiClient weatherApiClient;
    private final StringRedisTemplate redisTemplate; // Redis와 통신 (Key, Value가 모두 String일 때 사용)
    private final ObjectMapper objectMapper;     // JSON 문자열을 파싱 (spring-web이 기본 포함)
    private static final String CACHE_KEY_MAP_DATA = "weather:map:data";

    /**
     * DB의 모든 도시 날씨 정보를 갱신하여 Redis에 저장합니다.
     */
    /**
     * [수정] DB의 모든 도시 날씨 정보를 갱신하여 DB와 Redis에 모두 저장합니다.
     */
    @Transactional // [추가] DB 저장이 포함되므로 트랜잭션 처리
    public void updateAllCityWeather() {
        log.info("날씨 정보 갱신을 시작합니다...");
        List<City> cities = cityRepository.findAll();

        for (City city : cities) {
            String ptyValue = "0"; // 기본값
            try {
                // 1. 공공 API 호출 (재시도 로직 적용됨)
                String jsonResponse = weatherApiClient.getUltraShortTermWeather(city.getNx(), city.getNy());
                ptyValue = parsePtyValue(jsonResponse);

                log.info("[{}] 날씨 갱신 완료 (PTY: {})", city.getName(), ptyValue);

            } catch (Exception e) {
                log.error("[{}] 날씨 갱신 3회 재시도 모두 실패: {}", city.getName(), e.getMessage());
                // (실패 시 ptyValue는 "0" 유지)
                log.warn("[{}] 날씨 정보를 기본값(맑음)으로 저장합니다.", city.getName());

            } finally {
                // 2. [수정] MySQL DB에 ptyCode 저장
                //    (city 엔티티가 영속성 컨텍스트에 있으므로, set만 해도 update 쿼리 발생)
                city.setPtyCode(ptyValue);
                // cityRepository.save(city); // @Transactional이므로 save() 호출 안 해도 됨 (JPA Dirty Checking)

                // 3. [수정] Redis에도 갱신된 값 저장
                String redisKey = "weather::" + city.getId();
                redisTemplate.opsForValue().set(redisKey, ptyValue, 65, TimeUnit.MINUTES);

                // 4. [유지] Rate Limit을 위해 0.1초 대기
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.info("DB 갱신 완료. Redis 메인 캐시를 삭제합니다.");
        redisTemplate.delete(CACHE_KEY_MAP_DATA);
        log.info("날씨 정보 갱신을 완료했습니다.");
    }

    /**
     * [테스트 B용 최종 수정]
     * 지도에 표시할 날씨 데이터를 Redis가 아닌 'MySQL DB'에서 직접 조회합니다.
     * @return 23개 도시의 날씨 정보 리스트
     */
    public List<MapDataResponse> getMapWeatherData() {

        try {
            // 1. Redis에서 캐시된 '최종 결과물'(JSON 문자열)을 가져옵니다.
            String cachedData = redisTemplate.opsForValue().get(CACHE_KEY_MAP_DATA);

            if (cachedData != null) {
                // 2. Cache Hit: JSON 문자열을 List 객체로 변환하여 즉시 반환
                // (DB 호출이 여기서 0회 발생!)
                return objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, MapDataResponse.class));
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 읽기/파싱 실패: {}", e.getMessage());
            // 캐시 읽기에 실패해도, DB에서 가져오도록 로직을 계속 진행
        }
        log.info("Cache Miss: DB에서 Map 데이터를 조회합니다.");
        List<City> cities = cityRepository.findAll(); // DB 조회 1회
        List<MapDataResponse> mapDataList = cities.stream()
                .map(city -> {
                    String ptyCode = (city.getPtyCode() != null) ? city.getPtyCode() : "0";
                    WeatherStatus status = WeatherStatus.fromCode(ptyCode);
                    return new MapDataResponse(city, status, ptyCode);
                })
                .toList();

        // 4. [테스트 A]를 위해 DB 조회 결과를 Redis에 저장
        try {
            String jsonData = objectMapper.writeValueAsString(mapDataList);
            // 스케줄러(1시간) 주기보다 1분 짧은 59분(3540초) 동안 캐시 저장
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