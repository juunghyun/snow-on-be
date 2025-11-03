package com.snowon.snowon.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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
    @Retryable(
            value = { RestClientException.class }, // I/O error, Timeout 등 RestTemplate 관련 오류
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000) // 1초 후 재시도
    )
    public String getUltraShortTermWeather(int nx, int ny) {
        String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDateTime = now.getMinute() < 10 ? now.minusHours(1) : now;
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HH00"));

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000) // 넉넉하게
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        return restTemplate.getForObject(uri, String.class);
    }
}