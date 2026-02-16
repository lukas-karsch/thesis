import http from 'k6/http';
import {check, group} from "k6";
import {checkResponseIs200} from "../../helper/assert.js";
import {getVUS} from "../../helper/env.js";
import {createGrades} from "../../helper/create-grades.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

const VUS = getVUS(__ENV);

export const options = {
    scenarios: {
        gradeHistory: {
            executor: "ramping-arrival-rate",
            timeUnit: "1s",
            preAllocatedVUs: VUS,
            maxVus: VUS * 3,
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
    setupTimeout: '140s'
};

export function setup() {
    return createGrades(http, TARGET_HOST, true);
}

export default function (data) {
    const {studentIds, assessmentIds} = data;

    const studentId = studentIds[Math.floor(Math.random() * studentIds.length)];
    const assessmentId = assessmentIds[Math.floor(Math.random() * assessmentIds.length)].trim();

    group("Get grade history for a student and assessment", () => {
        const res = http.get(`${TARGET_HOST}/stats/grades/history?studentId=${studentId}&lectureAssessmentId=${assessmentId}`);
        checkResponseIs200(res);
        check(res, {
            "grade history shouldn't be empty": r => r.json().data.history.length > 0
        });
    });
}
