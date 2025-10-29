package com.snowon.snowon.controller;

import com.snowon.snowon.dto.RankingResult;
import com.snowon.snowon.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private static final int TOP_N = 5; // 상위 5개

    @GetMapping("/popular-cities")
    public ResponseEntity<List<RankingResult>> getPopularCities() {
        List<RankingResult> results = rankingService.getPopularCities(TOP_N);
        return ResponseEntity.ok(results);
    }
}