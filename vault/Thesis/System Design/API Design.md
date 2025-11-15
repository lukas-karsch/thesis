This file always contains the most current version of my endpoints.

## Included

- [[2025-11-09 API Design (2f6e551d)]]
- [[2025-11-13 Defining additional requirements (9aa823f1, 1c08c5be)]]

| endpoint                           | method   | query parameters               | returns                                                                       |
| ---------------------------------- | -------- | ------------------------------ | ----------------------------------------------------------------------------- |
| /courses                           | `GET`    |                                | 200, list of all courses                                                      |
| /courses                           | `POST`   |                                | 201, create a new course                                                      |
| /lectures                          | `GET`    | student_id                     | 200, list of all lectures the student is enrolled in                          |
| /lectures/create                   | `POST`   | course_id, professor_id        | 201, create a lecture from the course                                         |
| /lectures/lecture_id/enroll        | `POST`   | lectureId                      | 201                                                                           |
| /lectures/lecture_id/enroll        | `DELETE` | lectureId                      | 200                                                                           |
| /lectures/lecture_id               | GET      |                                | 200 return the lecture & enrolled students                                    |
| /lectures/lecture_id               | `POST`   | grade                          | 201, assign a grade to the student                                            |
| /lectures/lecture_id               | `PATCH`  | grade                          | 200, update a grade                                                           |
| /lectures/lecture_id/dates         | `POST`   |                                | 201, assign dates to a lecture                                                |
| /lectures/{lecture_id}/assessments | `POST`   |                                | 201, Add an assessment to a lecture                                           |
| /lectures/{lecture_id}/lifecycle   | `POST`   | newState                       | 201, advance the lifecycle of the lecture                                     |
| /lectures/{lecture_id}/waitingList | `GET`    |                                | 200, get the waiting list of a lecture                                        |
| /stats/credits                     | `GET`    |                                | 200, sum of all credits that the student accumulated through finished courses |
| /stats/grades                      | GET      |                                | 200, list of all grades the student has                                       |
| /stats/grades/history              | `GET`    | lecture_id, startDate, endDate | 200, list of all states of the grade throughout the time period.              |
