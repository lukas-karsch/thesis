import http from 'k6/http';
import {check, sleep} from 'k6';

// The host to target, configurable via an environment variable
const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

// Test options
export const options = {
    stages: [
        {duration: '30s', target: 20}, // Ramp-up to 20 virtual users over 30s
        {duration: '1m', target: 20},  // Stay at 20 virtual users for 1 minute
        {duration: '10s', target: 0},   // Ramp-down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'], // 95% of requests must complete below 500ms
        'http_req_failed': ['rate<0.01'],    // Error rate must be less than 1%
    },
};

const uuids = {}

const getUuid = (VU) => {
    if (!uuids[VU]) {
        uuids[VU] = crypto.randomUUID();
    }
    return uuids[VU];
}

export default function () {
    const url = `${TARGET_HOST}/courses`;

    const payload = JSON.stringify({
        "name": `Performance Test Course ${__VU}-${__ITER}`,
        "description": "A course created during performance testing.",
        "assessments": [{"weight": 1, "assessmentType": "PAPER"}],
        "credits": 5,
        "prerequisiteCourseIds": []
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${getUuid(__VU)}`
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'is status 200 or 201': (r) => r.status === 200 || r.status === 201,
    });

    sleep(1); // Wait for 1 second between requests per VU
}
