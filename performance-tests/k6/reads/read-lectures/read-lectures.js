import http from 'k6/http';
import {sleep} from "k6";
import {assertResponseIs201, checkResponseIs200} from "../../helper/assert.js";

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
    const courseLimit = 50

    //
    // CREATE COURSES
    //
    const createCourseUrl = `${TARGET_HOST}/courses`;

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
        firstName: "Lukas",
        lastName: "Karsch"
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
            const res = http.post(createCourseUrl, payload, professorParams)
            assertResponseIs201(res)
            const courseId = res.json().data
            console.log({courseId})
            courseIds.push(courseId)
        }
    );
    console.log(`Created ${courseLimit} courses`)

    setTime({
        "year": 2025,
        "month": 12,
        "dayOfMonth": 10,
        "hour": 10,
        "minutes": 0,
        "seconds": 0
    });

    //
    // CREATE LECTURES
    //

    const createLecturePayloads = courseIds.map((courseId, idx) => {
        // offset to avoid schedule conflicts (business rule)
        const scheduledDate = getOffsetDate("2025-12-15", idx);

        return JSON.stringify({
            courseId,
            maximumStudents: 5,
            dates: [{
                date: scheduledDate,
                startTime: "15:00",
                endTime: "17:00"
            }]
        });
    });

    const lectureIds = []
    createLecturePayloads.forEach(
        payload => {
            const res = http.post(`${TARGET_HOST}/lectures/create`, payload, professorParams)
            assertResponseIs201(res)
            const lectureId = res.json().data
            console.log({lectureId})
            lectureIds.push(lectureId)
        }
    );
    console.log(`Created a lecture per course.`)

    lectureIds.forEach(
        lectureId => {
            const res = http.post(`${TARGET_HOST}/lectures/${lectureId}/lifecycle?newLectureStatus=OPEN_FOR_ENROLLMENT`, undefined, professorParams)
            assertResponseIs201(res)
        }
    )
    console.log("Advanced lifecycle of all lectures to OPEN_FOR_ENROLLMENT")

    //
    // CREATE STUDENTS AND ENROLL
    //
    const studentLimit = 100
    const studentIds = [];
    for (let i = 0; i < studentLimit; i++) {
        const res = http.post(`${TARGET_HOST}/users/student`, JSON.stringify({
            firstName: "Student",
            lastName: `#${i}`
        }), {
            headers: {
                "Content-Type": "application/json"
            }
        })
        assertResponseIs201(res)
        studentIds.push(res.json().data)
    }

    for (let i = 0; i < studentLimit; i++) {
        const lectureId = lectureIds[i % lectureIds.length]
        const res = http.post(`${TARGET_HOST}/lectures/${lectureId}/enroll`, undefined, {
            headers: {
                'Content-Type': 'application/json',
                'customAuth': `student_${studentIds[i]}`
            },
        })
        assertResponseIs201(res)
    }
    console.log(`Enrolled ${studentLimit} students.`)

    return {
        courseIds,
        lectureIds,
        studentIds
    }
}

export default function (data) {
    const {
        courseIds,
        lectureIds,
        studentIds
    } = data

    // Pick a random student
    const pickedStudent = studentIds[Math.floor(Math.random() * studentIds.length)]
    // Fetch lectures for this student
    const res = http.get(`${TARGET_HOST}/lectures?studentId=${pickedStudent}`);
    checkResponseIs200(res);

    sleep(1);
}

const setTime = (newTime) => {
    const res = http.post(`${TARGET_HOST}/actuator/date-time`, JSON.stringify(newTime), {
        headers: {
            "Content-Type": "application/json"
        }
    });
    assertResponseIs201(res)
}

/**
 * Helper to shift a date by a specific number of days.
 * Automatically handles month-end (e.g., Dec 31 -> Jan 1).
 */
const getOffsetDate = (baseDateStr, dayOffset) => {
    const date = new Date(baseDateStr);

    // Increment the day by the index
    date.setDate(date.getDate() + dayOffset);

    // Returns YYYY-MM-DD
    return date.toISOString().split('T')[0];
};
