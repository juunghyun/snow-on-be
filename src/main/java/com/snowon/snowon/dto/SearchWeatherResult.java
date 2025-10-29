package com.snowon.snowon.dto;

import com.snowon.snowon.domain.WeatherStatus;
import lombok.Getter;

@Getter
public class SearchWeatherResult {
    private final Long cityId;
    private final String cityName;
    private final int nx;
    private final int ny;
    private final WeatherStatus weatherStatus; // 날씨 상태 추가!
    private final String ptyCode; // 원본 코드 (디버깅/프론트엔드용)

    public SearchWeatherResult(CitySearchResult cityInfo, WeatherStatus status, String ptyCode) {
        this.cityId = cityInfo.getCityId();
        this.cityName = cityInfo.getCityName();
        this.nx = cityInfo.getNx();
        this.ny = cityInfo.getNy();
        this.weatherStatus = status;
        this.ptyCode = ptyCode;
    }
}