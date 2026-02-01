import http from 'k6/http';
import {check, group, sleep} from "k6";
import {assertResponseIs201} from "../../helper/assert.js";
import {getVUS} from "../../helper/env.js";
import {getOffsetDate} from "../../helper/time.js";

const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';
const VUS = getVUS(__ENV);

const maxVUs = VUS;
const MAX_ITER_PER_VU = (80 * VUS + 20 * VUS) / maxVUs; // calculated from stages below.

export const options = {
    scenarios: {
        enrollmentConsistency: {
            executor: "ramping-arrival-rate",
            timeUnit: "1s",
            preAllocatedVUs: VUS,
            maxVUs,
            stages: [
                {target: VUS, duration: "20s"},
                {target: VUS, duration: "80s"},
                {target: 0, duration: "20s"}
            ]
        }
    },
    summaryTrendStats: ["med", "p(99)", "p(95)", "avg"],
};

export function setup() {
    const studentLimit = VUS;
    const lectureLimit = MAX_ITER_PER_VU;
    console.log(`Setting up test with ${lectureLimit} lectures and ${studentLimit} students.`);

    //
    // CREATE PROFESSOR
    //
    const createProfessorResponse = http.post(`${TARGET_HOST}/users/professor`, JSON.stringify({
        firstName: "Perf",
        lastName: "Test"
    }), {headers: {"Content-Type": "application/json"}});
    assertResponseIs201(createProfessorResponse);
    const professorId = createProfessorResponse.json().data;
    console.log(`Created professor with ID ${professorId}`);
    const professorParams = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${professorId}`
        },
    };

    //
    // CREATE COURSES
    //
    const courseIds = [];
    for (let i = 0; i < lectureLimit; i++) {
        const payload = JSON.stringify({
            "name": `Perf Test Course ${i}`,
            "description": "A course for performance testing.",
            "assessments": [{"weight": 1, "assessmentType": "EXAM"}],
            "credits": 5,
            "prerequisiteCourseIds": []
        });
        const res = http.post(`${TARGET_HOST}/courses`, payload, professorParams);
        assertResponseIs201(res);
        courseIds.push(res.json().data);
    }
    console.log(`Created ${courseIds.length} courses.`);
    sleep(1);

    // Set time to a fixed point to ensure lectures can be created reliably
    setTime({
        "year": 2026, "month": 1, "dayOfMonth": 1,
        "hour": 10, "minutes": 0, "seconds": 0
    });

    //
    // CREATE LECTURES
    //
    const lectureIds = [];
    for (let i = 0; i < lectureLimit; i++) {
        const scheduledDate = getOffsetDate("2026-02-01", i);
        const payload = JSON.stringify({
            courseId: courseIds[i],
            maximumStudents: studentLimit, // allow enough space for every student
            dates: [{
                date: scheduledDate,
                startTime: "10:00",
                endTime: "12:00"
            }]
        });
        const res = http.post(`${TARGET_HOST}/lectures/create`, payload, professorParams);
        assertResponseIs201(res);
        lectureIds.push(res.json().data);
    }
    console.log(`Created ${lectureIds.length} lectures.`);
    sleep(1);

    //
    // ADVANCE LECTURE LIFECYCLE
    //
    lectureIds.forEach(lectureId => {
        const res = http.post(`${TARGET_HOST}/lectures/${lectureId}/lifecycle?newLectureStatus=OPEN_FOR_ENROLLMENT`, undefined, professorParams);
        assertResponseIs201(res);
    });
    console.log("Advanced lifecycle of all lectures to OPEN_FOR_ENROLLMENT.");
    sleep(1);

    //
    // CREATE STUDENTS
    //
    const studentIds = [];
    for (let i = 0; i < studentLimit; i++) {
        const res = http.post(`${TARGET_HOST}/users/student`, JSON.stringify({
            firstName: "Student",
            lastName: `VU #${i}`
        }), {headers: {"Content-Type": "application/json"}});
        assertResponseIs201(res);
        studentIds.push(res.json().data);
    }
    console.log(`Created ${studentIds.length} students.`);

    return {lectureIds, studentIds};
}

export default function (data) {
    const {lectureIds, studentIds} = data;

    if (__ITER >= MAX_ITER_PER_VU) {
        console.error(`VU ${__VU} reached max iterations.`)
        return;
    }

    const studentId = studentIds[__VU - 1];
    const uniqueIndex = (__VU - 1 + __ITER) % MAX_ITER_PER_VU;
    const lectureId = lectureIds[uniqueIndex];

    if (studentId === undefined) {
        console.log(`studentId=undefined; __VU=${__VU}`)
    }

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `student_${studentId}`
        },
    }

    group("Enroll student to lecture", () => {
        const res = http.post(`${TARGET_HOST}/lectures/${lectureId}/enroll`, undefined, params);

        check(res, {
            'is status 201 (Created)': (r) => r.status === 201,
        });

        if (res.status !== 201) {
            console.error(`Failed request. params=${JSON.stringify(params)}; __VU=${__VU}; __ITER=${__ITER}`);
        }
    });
}


// --- HELPER FUNCTIONS ---

const setTime = (newTime) => {
    const res = http.post(`${TARGET_HOST}/actuator/date-time`, JSON.stringify(newTime), {
        headers: {"Content-Type": "application/json"}
    });
    assertResponseIs201(res);
};
