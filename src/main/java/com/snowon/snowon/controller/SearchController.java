package com.snowon.snowon.controller;

import com.snowon.snowon.dto.CitySearchResult;
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
@RequestMapping("/api/v1/search") // /api/v1/search 로 시작하는 요청 처리
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 도시 이름을 검색합니다 (오타 교정 포함).
     * @param query 검색어 (예: "슈원")
     * @return 검색된 도시 목록 (유사도 순)
     */
    @GetMapping // GET /api/v1/search?query=...
    public ResponseEntity<List<CitySearchResult>> searchCities(
            @RequestParam("query") String query // URL 파라미터 'query' 값을 받음
    ) {
        log.info("SearchController received query parameter: [{}]", query);
        // 1. SearchService에게 검색 요청 위임
        List<CitySearchResult> results = searchService.searchCity(query);

        // 2. 결과를 200 OK 응답으로 반환
        return ResponseEntity.ok(results);
    }
}