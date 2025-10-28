package com.snowon.snowon.dto;

import com.snowon.snowon.domain.City;
import com.snowon.snowon.domain.WeatherStatus;
import lombok.Getter;

@Getter
public class MapDataResponse {

    private final Long cityId;
    private final String cityName;
    private final WeatherStatus weatherStatus; // "SNOW", "CLEAR" 같은 Enum 값
    private final String ptyCode; // 디버깅용 원본 PTY 코드 ("3", "0")

    // 생성자
    public MapDataResponse(City city, WeatherStatus status, String ptyCode) {
        this.cityId = city.getId();
        this.cityName = city.getName();
        this.weatherStatus = status;
        this.ptyCode = ptyCode;
    }
}