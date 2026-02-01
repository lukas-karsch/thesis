import http from 'k6/http';
import {sleep} from "k6"
import {Rate} from "k6/metrics";
import {getVUS} from "../../helper/env.js";
import {setTime} from "../../helper/time.js";
import {assertResponseIs201} from "../../helper/assert.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

const VUS = getVUS(__ENV);

const readIsVisible = new Rate("read_visible_rate"); // described missing update within 100ms (defined by Freshness SLO)

export const options = {
    scenarios: {
        createLecture: {
            executor: "ramping-arrival-rate",
            timeUnit: "1s",
            preAllocatedVUs: VUS,
            maxVUs: Math.floor(VUS * 1.5),
            stages: [
                {target: VUS, duration: "20s"},
                {target: VUS, duration: "80s"},
                {target: 0, duration: "20s"}
            ]
        }
    },
    thresholds: {
        // add dummy thresholds so the two endpoints show up separately in the resulting JSON
        'http_req_duration{endpoint:/lectures/create}': ['avg>0'],
        'http_req_duration{endpoint:/lectures/{lectureId}}': ['avg>0'],
    },
    summaryTrendStats: ["med", "p(99)", "p(95)", "avg"],
};

export function setup() {
    const courseLimit = 50

    //
    // CREATE COURSES
    //
    const createCoursePayloads = []
    for (let i = 0; i < courseLimit; i++) {
        createCoursePayloads.push(JSON.stringify({
            "name": `Performance Test Course ${i}`,
            "description": "A course created during performance testing setup.",
            "assessments": [{"weight": 1, "assessmentType": "EXAM"}],
            "credits": 5,
            "prerequisiteCourseIds": []
        }));
    }

    const createProfessorResponse = http.post(`${TARGET_HOST}/users/professor`, JSON.stringify({
        firstName: "John",
        lastName: "Pork"
    }), {
        headers: {
            "Content-Type": "application/json"
        }
    })
    assertResponseIs201(createProfessorResponse)
    const professorId = createProfessorResponse.json().data
    console.log(`Created professor with ID ${professorId}`)

    const professorParams = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${professorId}`
        },
    };

    const courseIds = []
    createCoursePayloads.forEach(
        payload => {
            const res = http.post(`${TARGET_HOST}/courses`, payload, professorParams)
            assertResponseIs201(res)
            const courseId = res.json().data
            courseIds.push(courseId)
        }
    );
    console.log(`Created ${courseLimit} courses`)
    sleep(1);

    setTime({
        "year": 2025,
        "month": 12,
        "dayOfMonth": 10,
        "hour": 10,
        "minutes": 0,
        "seconds": 0
    }, http, TARGET_HOST);

    return {
        courseIds,
        professorId,
    }
}

export default function (data) {
    const {courseIds, professorId} = data;

    const courseId = courseIds[Math.floor(Math.random() * courseIds.length)];

    const professorParams = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${professorId}`
        },
        tags: {
            endpoint: "/lectures/create"
        }
    };

    const payload = JSON.stringify({
        courseId,
        maximumStudents: 5,
        dates: [{
            date: "2025-12-15",
            startTime: "15:00",
            endTime: "17:00"
        }]
    });

    const res = http.post(`${TARGET_HOST}/lectures/create`, payload, professorParams);
    assertResponseIs201(res);
    const lectureId = res.json().data;

    sleep(0.1)
    const pollingResponse = http.get(`${TARGET_HOST}/lectures/${lectureId}`, {
        tags: {
            endpoint: "/lectures/{lectureId}"
        }
    });
    if (pollingResponse.status === 200) {
        readIsVisible.add(true);
    } else {
        readIsVisible.add(false);
    }
}
