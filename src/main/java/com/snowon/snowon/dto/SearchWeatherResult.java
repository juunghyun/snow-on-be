package com.snowon.snowon.dto;

import com.snowon.snowon.domain.WeatherStatus;
import lombok.Getter;

@Getter
public class SearchWeatherResult {
    private final Long cityId;
    private final String cityName;
    private final int nx;
    private final int ny;
    private final WeatherStatus weatherStatus;
    private final String ptyCode;

    public SearchWeatherResult(CitySearchResult cityInfo, WeatherStatus status, String ptyCode) {
        this.cityId = cityInfo.getCityId();
        this.cityName = cityInfo.getCityName();
        this.nx = cityInfo.getNx();
        this.ny = cityInfo.getNy();
        this.weatherStatus = status;
        this.ptyCode = ptyCode;
    }
}