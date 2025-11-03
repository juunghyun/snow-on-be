// 파일 이름: test.js

import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    // 10명의 가상 유저(VUs)가 동시에 요청
    vus: 10,
    // 20초 동안 테스트 실행
    duration: '20s',
};

export default function () {
    // 1. 테스트 대상: 인텔리제이에서 실행 중인 로컬 BE API
    const res = http.get('http://localhost:8080/api/v1/weather/map-data');

    // 2. (선택적) 1초 대기 (실제 사용자와 유사하게)
    // [테스트 B] (공공 API)에서는 이 sleep을 주석 처리하는 것이 좋습니다.
    // [테스트 A] (Redis)에서는 이 sleep을 활성화해야 Redis에 과부하를 주지 않습니다.
    // sleep(1);
}