package com.snowon.snowon.controller;

import com.snowon.snowon.dto.MapDataResponse;
import com.snowon.snowon.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // 이 클래스는 REST API 컨트롤러임을 선언
@RequestMapping("/api/v1/weather") // /api/v1/weather 로 시작하는 모든 요청을 받음
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * 지도 시각화를 위한 전체 도시의 날씨 정보를 반환합니다.
     */
    @GetMapping("/map-data")
    public ResponseEntity<List<MapDataResponse>> getMapData() {
        List<MapDataResponse> mapData = weatherService.getMapWeatherData();
        return ResponseEntity.ok(mapData); // 200 OK 응답과 함께 JSON 바디 반환
    }
}