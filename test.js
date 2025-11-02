// 파일 이름: test.js

import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 10, // 가상 유저 10명
    duration: '2s', // 30초 동안 테스트
};

export default function () {
    http.get('http://localhost:8080/api/v1/weather/map-data');

    // sleep(1);
}