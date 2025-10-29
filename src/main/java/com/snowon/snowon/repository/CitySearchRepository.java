package com.snowon.snowon.repository;

import com.snowon.snowon.domain.CitySearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// ElasticsearchRepository<[도큐먼트 클래스], [ID 타입]>
// Spring Data Elasticsearch가 save(), saveAll(), findById(), search() 등
// 기본 메서드를 자동으로 구현해 줍니다.
public interface CitySearchRepository extends ElasticsearchRepository<CitySearchDocument, Long> {


}