import {sleep} from "k6";

import {getOffsetDate, setTime} from "./time.js";
import {assertResponseIs200, assertResponseIs201} from "./assert.js";
import {createProfessor} from "./professor.js";

export function createGrades(http, targetHost, withUpdate) {
    setTime({
        "year": 2025,
        "month": 12,
        "dayOfMonth": 10,
        "hour": 10,
        "minutes": 0,
        "seconds": 0
    }, http, targetHost);

    const studentLimit = 50
    const studentIds = [];
    for (let i = 0; i < studentLimit; i++) {
        const res = http.post(`${targetHost}/users/student`, JSON.stringify({
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

    // 2. Create Professor

    const professorId = createProfessor(http, {firstName: "John", lastName: "Pork"}, targetHost);

    const professorParams = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': `professor_${professorId}`
        },
    };

    // 3. Create courses, each worth 5 credits
    const courseLimit = 10

    const createCoursePayloads = []
    for (let i = 0; i < courseLimit; i++) {
        createCoursePayloads.push(JSON.stringify({
            "name": `Performance Test Course ${i}`,
            "description": "A course created during performance testing setup.",
            "assessments": [
                {"weight": .5, "assessmentType": "EXAM"},
                {"weight": .5, "assessmentType": "PAPER"}
            ],
            "credits": 5,
            "prerequisiteCourseIds": []
        }));
    }

    const courseIds = []
    createCoursePayloads.forEach(
        payload => {
            const res = http.post(`${targetHost}/courses`, payload, professorParams)
            assertResponseIs201(res)
            const courseId = res.json().data
            courseIds.push(courseId)
        }
    );
    console.log(`Created ${courseLimit} courses`)
    sleep(1)

    // 4. Create lectures for the courses

    const createLecturePayloads = courseIds.map((courseId, idx) => {
        // offset to avoid schedule conflicts (business rule)
        const scheduledDate = getOffsetDate("2025-12-10", idx);

        return JSON.stringify({
            courseId,
            maximumStudents: 100,
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
            const res = http.post(`${targetHost}/lectures/create`, payload, professorParams)
            assertResponseIs201(res)
            const lectureId = res.json().data
            lectureIds.push(lectureId)
        }
    );
    console.log(`Created a lecture per course.`)
    sleep(1)

    // 4.1 Set lifecycle to OPEN_FOR_ENROLLMENT
    lectureIds.forEach(
        lectureId => {
            const res = http.post(`${targetHost}/lectures/${lectureId}/lifecycle?newLectureStatus=OPEN_FOR_ENROLLMENT`, undefined, professorParams)
            assertResponseIs201(res)
        }
    )
    console.log("Advanced lifecycle of all lectures to OPEN_FOR_ENROLLMENT")
    sleep(1)

    // 5. Enroll each student
    for (const studentId of studentIds) {
        for (const lectureId of lectureIds) {
            const res = http.post(`${targetHost}/lectures/${lectureId}/enroll`, undefined, {
                headers: {
                    'Content-Type': 'application/json',
                    'customAuth': `student_${studentId}`
                },
            })
            assertResponseIs201(res)
        }
    }
    console.log(`Enrolled ${studentLimit} students to ${courseLimit} lectures.`)

    // 5.1 Lecture lifecycle to IN_PROGRESS
    lectureIds.forEach(
        lectureId => {
            const res = http.post(`${targetHost}/lectures/${lectureId}/lifecycle?newLectureStatus=IN_PROGRESS`, undefined, professorParams)
            assertResponseIs201(res)
        }
    )
    console.log("Advanced lifecycle of all lectures to IN_PROGRESS")
    sleep(1)

    // 6. Create 2 assessments per lecture
    const assessmentDateDay = getOffsetDate("2025-12-10", lectureIds.length)
    const assessmentDate = {
        date: assessmentDateDay,
        startTime: "10:00",
        endTime: "12:00"
    }
    const lectureIdsToAssessments = new Map();
    lectureIds.forEach(lectureId => {
        const assessments = []
        const res1 = http.post(`${targetHost}/lectures/${lectureId}/assessments`, JSON.stringify({
                assessmentType: "EXAM", weight: 0.5,
                timeSlot: assessmentDate
            }),
            professorParams);
        assertResponseIs201(res1);
        assessments.push(res1.json().data);

        const res2 = http.post(`${targetHost}/lectures/${lectureId}/assessments`, JSON.stringify({
                assessmentType: "EXAM", weight: 0.5,
                timeSlot: assessmentDate
            }),
            professorParams);
        assertResponseIs201(res2);
        assessments.push(res2.json().data);

        lectureIdsToAssessments.set(lectureId, assessments);
    });
    console.log("Created 2 assessments per lecture");

    const finalTime = new Date(getOffsetDate(assessmentDateDay, 2))
    setTime({
        "year": finalTime.getFullYear(),
        "month": finalTime.getMonth() + 1, // months are 0-indexed
        "dayOfMonth": finalTime.getDate(),
        "hour": 10,
        "minutes": 0,
        "seconds": 0
    }, http, targetHost);

    // 7. Assign grades for each student and every assessment
    const grade = 100
    lectureIdsToAssessments.forEach((assessments, lectureId) => {
        assessments.forEach(assessmentId => {
            studentIds.forEach(studentId => {
                const res = http.post(`${targetHost}/lectures/${lectureId}`, JSON.stringify({
                    studentId,
                    assessmentId,
                    grade
                }), professorParams);
                assertResponseIs201(res);
            });
        })
    });

    const updateGrade = 80
    if (withUpdate) {
        console.log("Updating all grades.");
        lectureIdsToAssessments.forEach((assessments, lectureId) => {
            assessments.forEach(assessmentId => {
                studentIds.forEach(studentId => {
                    const res = http.patch(`${targetHost}/lectures/${lectureId}`, JSON.stringify({
                        studentId,
                        assessmentId,
                        grade: updateGrade
                    }), professorParams);
                    assertResponseIs200(res);
                });
            })
        });
    }

    // Set lifecycle to finished
    lectureIds.forEach(
        lectureId => {
            const res = http.post(`${targetHost}/lectures/${lectureId}/lifecycle?newLectureStatus=FINISHED`, undefined, professorParams)
            assertResponseIs201(res)
        }
    )
    console.log("Advanced lifecycle of all lectures to FINISHED");

    console.log("Sleeping...");
    sleep(15);

    console.log("Setup complete.");

    const assessmentIds = [...lectureIdsToAssessments.values()].flat();

    return {
        studentIds,
        assessmentIds
    };
}
