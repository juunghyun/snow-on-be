package com.snowon.snowon.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.snowon.snowon.domain.CitySearchDocument;
import com.snowon.snowon.domain.WeatherStatus;
import com.snowon.snowon.dto.CitySearchResult;
import com.snowon.snowon.dto.SearchWeatherResult;
import com.snowon.snowon.repository.CitySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CitySearchRepository citySearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate redisTemplate; // RedisTemplate 주입 추가!

    /**
     * 키워드로 도시를 검색하고 날씨 정보를 포함하여 반환합니다.
     * @param keyword 검색어
     * @return 검색 결과 (도시 정보 + 날씨) 리스트 (최대 1개)
     */
    public List<SearchWeatherResult> searchCityWithWeather(String keyword) { // 메서드 이름 변경 및 반환 타입 변경
        log.info("도시 검색(날씨포함) 요청: {}", keyword);

        if (keyword == null || keyword.isBlank()) {
            log.warn("검색어가 비어있습니다.");
            return Collections.emptyList();
        }

        // 1. ES 검색 (기존 로직과 동일)
        Query esQuery = Query.of(q -> q.match(m -> m.field("name").query(keyword).fuzziness("2")));
        NativeQuery nativeQuery = new NativeQueryBuilder().withQuery(esQuery).withPageable(PageRequest.of(0, 1)).build();
        SearchHits<CitySearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, CitySearchDocument.class);

        // 검색 결과 없으면 빈 리스트 반환
        if (searchHits.isEmpty()) {
            log.info("도시 검색 결과 없음");
            return Collections.emptyList();
        }

        // 2. ES 결과에서 City 정보 추출 (상위 1개만)
        CitySearchDocument foundCityDoc = searchHits.getSearchHits().get(0).getContent();
        CitySearchResult cityInfo = new CitySearchResult(foundCityDoc); // 기존 DTO 활용

        // 3. Redis에서 날씨 정보 조회
        String redisKey = "weather::" + cityInfo.getCityId();
        String ptyCode = redisTemplate.opsForValue().get(redisKey);
        ptyCode = (ptyCode == null) ? "0" : ptyCode; // Redis에 값 없으면 "0"(맑음) 처리
        WeatherStatus status = WeatherStatus.fromCode(ptyCode);

        // 4. 도시 정보 + 날씨 정보 결합하여 최종 결과 생성
        SearchWeatherResult result = new SearchWeatherResult(cityInfo, status, ptyCode);

        // 5. [기능 2 준비] 검색 성공 시 인기 검색어 점수 증가 (아래에서 구현)
        incrementSearchScore(cityInfo.getCityName());

        log.info("도시 검색(날씨포함) 결과 반환: {}", result.getCityName());
        return List.of(result); // 결과를 리스트에 담아 반환
    }

    // 인기 검색어 점수 증가 로직
    private void incrementSearchScore(String cityName) {
        try {
            // "popular_cities" 라는 Sorted Set에 cityName의 점수를 1 증가시킴
            redisTemplate.opsForZSet().incrementScore("popular_cities", cityName, 1.0);
            log.debug("[{}] 인기 검색어 점수 증가", cityName);
        } catch (Exception e) {
            // 랭킹 집계 실패가 검색 기능 자체에 영향을 주면 안 되므로 에러 로깅만 함
            log.error("인기 검색어 점수 증가 중 에러 발생: {}", e.getMessage());
        }
    }
}