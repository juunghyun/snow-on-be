package com.snowon.snowon.dto;

import com.snowon.snowon.domain.CitySearchDocument;
import lombok.Getter;

@Getter
public class CitySearchResult {
    private final Long cityId;
    private final String cityName;
    private final int nx;
    private final int ny;

    // CitySearchDocument를 받아서 CitySearchResult로 변환하는 생성자
    public CitySearchResult(CitySearchDocument document) {
        this.cityId = document.getId();
        this.cityName = document.getName();
        this.nx = document.getNx();
        this.ny = document.getNy();
    }
}