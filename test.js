// test.js

import http from 'k6/http';
import { sleep } from 'k6';

export const options = { vus: 10, duration: '20s' };
export default function () {
    http.get('http://localhost:8080/api/v1/weather/map-data');
}