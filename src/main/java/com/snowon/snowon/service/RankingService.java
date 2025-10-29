package com.snowon.snowon.service;

import com.snowon.snowon.dto.RankingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple; // TypedTuple import
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final StringRedisTemplate redisTemplate;
    private static final String RANKING_KEY = "popular_cities"; // Sorted Set 키 이름

    /**
     * 인기 검색어 상위 N개를 조회합니다.
     * @param count 조회할 개수 (예: 5)
     * @return 랭킹 리스트 (점수 내림차순)
     */
    public List<RankingResult> getPopularCities(int count) {
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, count - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of(); // 결과 없으면 빈 리스트 반환
        }

        // TypedTuple (value=도시명, score=점수) -> RankingResult DTO 변환
        return tuples.stream()
                .map(tuple -> new RankingResult(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toList());
    }
}