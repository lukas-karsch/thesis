import {sleep} from "k6";

import {createProfessor} from "./professor.js";
import {assertResponseIs201} from "./assert.js";
import {getOffsetDate, setTime} from "./time.js";

export function createLecturesAndEnroll(courseLimit, studentLimit, http, TARGET_HOST) {
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

    const professorId = createProfessor(http, {
        firstName: "Lukas",
        lastName: "Karsch"
    }, TARGET_HOST)

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
    console.log(`Created ${courseLimit} courses`);
    sleep(1);

    setTime({
        "year": 2025,
        "month": 12,
        "dayOfMonth": 10,
        "hour": 10,
        "minutes": 0,
        "seconds": 0
    }, http, TARGET_HOST);

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
            lectureIds.push(lectureId)
        }
    );
    console.log(`Created a lecture per course.`)
    sleep(1)

    lectureIds.forEach(
        lectureId => {
            const res = http.post(`${TARGET_HOST}/lectures/${lectureId}/lifecycle?newLectureStatus=OPEN_FOR_ENROLLMENT`, undefined, professorParams)
            assertResponseIs201(res)
        }
    )
    console.log("Advanced lifecycle of all lectures to OPEN_FOR_ENROLLMENT")
    sleep(1)

    //
    // CREATE STUDENTS AND ENROLL
    //
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
