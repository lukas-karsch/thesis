import http from 'k6/http';
import {group, sleep} from "k6";
import {checkResponseIs200} from "../../helper/assert.js";
import {getVUS} from "../../helper/env.js";
import {createLecturesAndEnroll} from "../../helper/lectures-seed-data.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

const VUS = getVUS(__ENV);

export const options = {
    scenarios: {
        getLectures: {
            executor: "ramping-arrival-rate",
            timeUnit: "1s",
            preAllocatedVUs: VUS,
            maxVUs: VUS * 2,
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

export function setup() {
    createLecturesAndEnroll(25, 15, http, TARGET_HOST);
    sleep(10);
}

export default function () {
    group("Read all lectures", () => {
        const res = http.get(`${TARGET_HOST}/lectures/all`);
        checkResponseIs200(res);
    });
}
