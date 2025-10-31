package com.snowon.snowon.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherApiClient {

    // application-secret.yml에서 API 키를 주입
    @Value("${api.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate;

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

        //현재 시각의 "분"을 기준으로 45분 이전, 이후인지 파악 후 파싱.
        // (45분 이전에 요청하면 1시간 전 데이터를 요청해야 함)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDateTime = now.getMinute() < 45 ? now.minusHours(1) : now;
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HH00"));

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("dataType", "JSON")
                .queryParam("numOfRows", 1000) // 넉넉하게
                .queryParam("pageNo", 1)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        return restTemplate.getForObject(uri, String.class);
    }
}