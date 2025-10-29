package com.snowon.snowon.dto;

import com.snowon.snowon.domain.CitySearchDocument;
import lombok.Getter;

@Getter
public class CitySearchResult {
    private final Long cityId;
    private final String cityName;
    private final int nx;
    private final int ny;
    // (선택사항) 검색 점수 (얼마나 정확하게 일치하는지)
    // private final float score;

    // CitySearchDocument를 받아서 CitySearchResult로 변환하는 생성자
    public CitySearchResult(CitySearchDocument document) {
        this.cityId = document.getId();
        this.cityName = document.getName();
        this.nx = document.getNx();
        this.ny = document.getNy();
        // this.score = score; // 나중에 필요하면 점수도 포함
    }
}