package com.snowon.snowon.dto;

import lombok.Getter;

@Getter
public class RankingResult {
    private final String cityName;
    private final double score; // 검색 횟수 (Redis Sorted Set은 double 타입 사용)

    public RankingResult(String cityName, double score) {
        this.cityName = cityName;
        this.score = score;
    }
}