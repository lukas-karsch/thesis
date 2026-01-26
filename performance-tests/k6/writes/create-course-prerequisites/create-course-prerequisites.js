import http from 'k6/http';
import {getUuidForVu} from "../../helper/uuids.js";
import {checkResponseIs201} from "../../helper/assert.js";
import {getVUS} from "../../helper/env.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

const VUS = getVUS(__ENV);

export const options = {
    scenarios: {
        createCourses: {
            executor: "ramping-arrival-rate",
            timeUnit: "1s",
            preAllocatedVUs: VUS,
            stages: [
                {target: VUS, duration: "20s"},
                {target: VUS, duration: "80s"},
                {target: 0, duration: "20s"}
            ]
        }
    },
    thresholds: {
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
            checkResponseIs201(res)
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

    checkResponseIs201(res);
}
