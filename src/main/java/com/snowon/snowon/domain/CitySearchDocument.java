package com.snowon.snowon.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
// ES에 "city_search"라는 이름의 인덱스(DB의 '테이블' 개념)를 생성
// settingPath를 지정하여 nori 분석기를 사용하도록 설정 파일을 참조
@Document(indexName = "city_search")
@Setting(settingPath = "elasticsearch/nori-settings.json") // nori 분석기 설정 파일 경로
public class CitySearchDocument {

    @Id
    @Field(type = FieldType.Keyword) // 정확히 일치(검색X, ID용)
    private final Long id; // MySQL의 city_id를 그대로 사용

    // FieldType.Text는 기본적으로 검색 가능한 텍스트 필드
    // 분석기는 @Setting에서 지정한 nori를 사용하게 됨
    @Field(type = FieldType.Text)
    private final String name; // 도시 이름 (예: "서울", "수원")

    @Field(type = FieldType.Integer)
    private final int nx;

    @Field(type = FieldType.Integer)
    private final int ny;

    @Builder
    public CitySearchDocument(Long id, String name, int nx, int ny) {
        this.id = id;
        this.name = name;
        this.nx = nx;
        this.ny = ny;
    }

    // MySQL의 'City' 엔티티를 ES의 'CitySearchDocument'로 변환하는 팩토리 메서드
    public static CitySearchDocument from(City city) {
        return CitySearchDocument.builder()
                .id(city.getId())
                .name(city.getName())
                .nx(city.getNx())
                .ny(city.getNy())
                .build();
    }
}