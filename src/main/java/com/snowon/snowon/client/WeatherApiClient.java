package com.snowon.snowon.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component // Spring이 이 클래스를 Bean으로 관리하도록 등록
public class WeatherApiClient {

    // application-secret.yml에 숨겨둔 API 키를 주입받음
    @Value("${api.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate;

    // 생성자 주입 (RestTemplate은 나중에 Bean으로 등록해야 함)
    public WeatherApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 특정 도시의 '초단기 실황' 날씨 데이터를 요청
     *
     * @param nx X좌표
     * @param ny Y좌표
     * @return API 응답 (JSON 문자열)
     */
    public String getUltraShortTermWeather(int nx, int ny) {
        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        // 1. Postman에서 했던 'base_date'와 'base_time' 계산
        // (45분 이전에 요청하면 1시간 전 데이터를 요청해야 함)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDateTime = now.getMinute() < 45 ? now.minusHours(1) : now;
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HH00"));

        // 2. URI 빌드 (Postman의 Params 설정과 동일)
        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("dataType", "JSON")
                .queryParam("numOfRows", 1000) // 넉넉하게
                .queryParam("pageNo", 1)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true) // serviceKey의 인코딩 문제 방지
                .toUri();

        // 3. API 호출 (GET 요청)
        // (에러 처리, DTO 파싱 등은 나중에 추가)
        return restTemplate.getForObject(uri, String.class);
    }
}