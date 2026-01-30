import http from 'k6/http';
import {group} from 'k6';
import {getUuidForVu} from "../../helper/uuids.js";
import {getVUS} from "../../helper/env.js";
import {checkResponseIs201} from "../../helper/assert.js";

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
    summaryTrendStats: ["med", "p(99)", "p(95)", "avg"],
};

const uuids = {}

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
            'customAuth': `professor_${getUuidForVu(__VU, uuids)}`
        },
    };

    group("Create courses without prerequisites", () => {
        const res = http.post(url, payload, params);
        checkResponseIs201(res);
    })
}
