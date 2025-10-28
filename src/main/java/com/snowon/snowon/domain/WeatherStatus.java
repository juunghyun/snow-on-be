package com.snowon.snowon.domain;

import java.util.Arrays;

public enum WeatherStatus {
    CLEAR("0"),        // 맑음 (강수 없음)
    RAIN("1"),        // 비
    RAIN_SNOW("2"),   // 비/눈
    SNOW("3"),        // 눈
    RAIN_DROP("5"),   // 빗방울
    RAIN_SNOW_FLURRY("6"), // 빗방울/눈날림
    SNOW_FLURRY("7"),      // 눈날림
    UNKNOWN("-999");  // 결측치 또는 에러

    private final String code;

    WeatherStatus(String code) {
        this.code = code;
    }

    // "3"이라는 코드가 들어오면 WeatherStatus.SNOW를 반환하는 팩토리 메서드
    public static WeatherStatus fromCode(String code) {
        return Arrays.stream(WeatherStatus.values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElse(CLEAR); // 알 수 없는 코드는 '맑음'으로 처리
    }
}