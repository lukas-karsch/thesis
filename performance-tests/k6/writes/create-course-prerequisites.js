import http from 'k6/http';
import {check, sleep} from 'k6';
import {getUuidForVu} from "../helper/uuids.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

export const options = {
    stages: [
        {duration: '10s', target: 20}, // Ramp-up to 20 virtual users over 30s
        {duration: '1m', target: 20},  // Stay at 20 virtual users for 1 minute
        {duration: '10s', target: 0},   // Ramp-down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'], // 95% of requests must complete below 500ms
        'http_req_failed': ['rate<0.01'],    // Error rate must be less than 1%
    },
};

export function setup() {
    const limit = 10

    const prerequisiteIds = []

    console.log("-----")
    console.log("Setup")
    console.log(`Creating ${limit} courses`)
    console.log("-----")

    const url = `${TARGET_HOST}/courses`;

    const payloads = []
    for (let i = 0; i < limit; i++) {
        payloads.push(JSON.stringify({
            "name": `(Prerequisite) Performance Test Course ${i}`,
            "description": "A course created during performance testing setup.",
            "assessments": [{"weight": 1, "assessmentType": "EXAM"}],
            "credits": 5,
            "prerequisiteCourseIds": []
        }));
    }

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${crypto.randomUUID()}`
        },
    };

    payloads.forEach(
        p => {
            const res = http.post(url, p, params)
            assertResponseIs201(res)
            const uuid = res.json().data
            console.log({uuid})
            prerequisiteIds.push(uuid)
        }
    );
    console.log("-----")
    console.log("Setup finished")
    console.log("-----")

    return {
        prerequisiteIds
    }
}

const uuids = {}

export default function (data) {
    const {prerequisiteIds} = data

    const url = `${TARGET_HOST}/courses`;

    const numberOfPrerequisites = Math.floor(Math.random() * 5)
    const ids = []
    for (let i = 0; i < numberOfPrerequisites; i++) {
        const idx = Math.floor(Math.random() * prerequisiteIds.length)
        ids.push(prerequisiteIds[idx])
    }

    const payload = JSON.stringify({
        "name": `Performance Test Course ${__VU}-${__ITER}`,
        "description": "A course with prerequisites created during performance testing.",
        "assessments": [{"weight": 1, "assessmentType": "PAPER"}],
        "credits": 5,
        "prerequisiteCourseIds": ids
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${getUuidForVu(__VU, uuids)}`
        },
    };

    const res = http.post(url, payload, params);

    assertResponseIs201(res)

    sleep(1); // Wait for 1 second between requests per VU
}

const assertResponseIs201 = res => check(res, {
    'is status 201': (r) => r.status === 201,
})
