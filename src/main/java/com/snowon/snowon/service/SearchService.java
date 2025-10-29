package com.snowon.snowon.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.snowon.snowon.domain.CitySearchDocument;
import com.snowon.snowon.dto.CitySearchResult;
import com.snowon.snowon.repository.CitySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CitySearchRepository citySearchRepository; // ES Repository (기본 CRUD용)
    private final ElasticsearchOperations elasticsearchOperations; // 더 복잡한 ES 쿼리용

    /**
     * 키워드로 도시를 검색합니다 (오타 교정 포함).
     *
     * @param keyword 검색어 (예: "슈원")
     * @return 검색 결과 리스트 (가장 유사한 순서)
     */
    public List<CitySearchResult> searchCity(String keyword) {
        log.info("도시 검색 요청: {}", keyword);

        if (keyword == null || keyword.isBlank()) {
            log.warn("검색어가 비어있습니다.");
            return Collections.emptyList(); // 빈 리스트 즉시 반환
        }

        // 1. Elasticsearch 쿼리 생성 (Match Query + Fuzziness)
        Query esQuery = Query.of(q -> q
                .match(m -> m
                        .field("name") // 'name' 필드에서 검색
                        .query(keyword) // 사용자가 입력한 키워드
                        .fuzziness("2") // 오타 자동 교정 (예: 1~2글자 차이 허용)
                        .operator(Operator.And) // 모든 단어가 포함되어야 함 (선택사항)
                )
        );

        // NativeQuery: Spring Data ES가 제공하는 유연한 쿼리 방식
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(esQuery)
                 .withPageable(PageRequest.of(0, 1)) // 가장 유사한 1등 항목만 검색되도록.
                .build();

        // 2. ElasticsearchOperations를 사용하여 검색 실행
        SearchHits<CitySearchDocument> searchHits = elasticsearchOperations.search(nativeQuery, CitySearchDocument.class);

        // 3. 검색 결과를 CitySearchResult DTO로 변환
        List<CitySearchResult> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent) // CitySearchDocument 객체만 추출
                .map(CitySearchResult::new) // DTO로 변환
                .collect(Collectors.toList());

        log.info("도시 검색 결과 {}건 반환", results.size());
        return results;
    }
}