package com.snowon.snowon.controller;

import com.snowon.snowon.dto.SearchWeatherResult;
import com.snowon.snowon.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 도시 이름을 검색합니다 (오타 교정 포함).
     * @param query 검색어 (예: "슈원")
     * @return 검색된 도시 목록 (유사도 순)
     */
    @GetMapping
    public ResponseEntity<List<SearchWeatherResult>> searchCitiesWithWeather(@RequestParam("query") String query
    ) {
        log.info("SearchController received query parameter: [{}]", query);
        List<SearchWeatherResult> results = searchService.searchCityWithWeather(query);
        return ResponseEntity.ok(results);
    }
}