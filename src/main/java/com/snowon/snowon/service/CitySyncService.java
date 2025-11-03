package com.snowon.snowon.service;

import com.snowon.snowon.domain.City;
import com.snowon.snowon.domain.CitySearchDocument;
import com.snowon.snowon.repository.CityRepository;
import com.snowon.snowon.repository.CitySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitySyncService {

    private final CityRepository cityRepository; // MySQL과 통신
    private final CitySearchRepository citySearchRepository; // ES와 통신

    /**
     * MySQL의 모든 도시 데이터를 Elasticsearch로 동기화(복사)합니다.
     */
    @Transactional(readOnly = true) // MySQL은 읽기 전용
    public void syncCitiesToElasticsearch() {
        log.info("MySQL -> Elasticsearch 데이터 동기화를 시작합니다...");


        List<City> cities = cityRepository.findAll();
        List<CitySearchDocument> documents = cities.stream()
                .map(CitySearchDocument::from) // CitySearchDocument에 만든 팩토리 메서드 사용
                .toList();

        citySearchRepository.saveAll(documents);

        log.info("ES 데이터 동기화 완료. 총 {}개 도시 저장.", documents.size());
    }
}