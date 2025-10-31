package com.snowon.snowon.repository;

import com.snowon.snowon.domain.CitySearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface CitySearchRepository extends ElasticsearchRepository<CitySearchDocument, Long> {


}