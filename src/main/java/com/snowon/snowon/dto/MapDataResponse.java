package com.snowon.snowon.dto;

import com.fasterxml.jackson.annotation.JsonCreator; // [추가]
import com.fasterxml.jackson.annotation.JsonProperty; // [추가]
import com.snowon.snowon.domain.City;
import com.snowon.snowon.domain.WeatherStatus;
import lombok.Getter;

@Getter
public class MapDataResponse {

    private final Long cityId;
    private final String cityName;
    private final WeatherStatus weatherStatus; // "SNOW", "CLEAR" 같은 Enum 값
    private final String ptyCode; // 디버깅용 원본 PTY 코드 ("3", "0")

    // -----------------------------------------------------------------
    // [생성자 1] DB 조회 시 사용 (Cache Miss 시)
    // -----------------------------------------------------------------
    public MapDataResponse(City city, WeatherStatus status, String ptyCode) {
        this.cityId = city.getId();
        this.cityName = city.getName();
        this.weatherStatus = status;
        this.ptyCode = ptyCode;
    }

    // -----------------------------------------------------------------
    // [생성자 2 - 추가] Redis 조회 (JSON 역직렬화) 시 사용
    // @JsonCreator: ObjectMapper에게 이 생성자를 사용하라고 지시
    // @JsonProperty: JSON의 필드명을 이 파라미터에 매핑
    // -----------------------------------------------------------------
    @JsonCreator
    public MapDataResponse(
            @JsonProperty("cityId") Long cityId,
            @JsonProperty("cityName") String cityName,
            @JsonProperty("weatherStatus") WeatherStatus weatherStatus,
            @JsonProperty("ptyCode") String ptyCode
    ) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.weatherStatus = weatherStatus;
        this.ptyCode = ptyCode;
    }
}