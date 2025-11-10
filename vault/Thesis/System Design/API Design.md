As discussed in [[2025-11-09 API Design (2f6e551d)]], this will be the endpoints: 

| endpoint              | method | query parameters                           | returns                                                                       |
| --------------------- | ------ | ------------------------------------------ | ----------------------------------------------------------------------------- |
| /courses              | GET    |                                            | 200, list of all courses                                                      |
| /courses              | POST   |                                            | 201, create a new course                                                      |
| /lectures             | GET    | student_id                                 | 200, list of all lectures the student is enrolled in                          |
| /lectures/create      | POST   | course_id, professor_id                    | 201, create a lecture from the course                                         |
| /lectures/enroll      | `POST` | studentId                                  | 201                                                                           |
| /lectures/lecture_id  | GET    |                                            | 200 return the lecture & enrolled students                                    |
| /lectures/lecture_id  | POST   | student_id, professor_id, grade            | 201, assign a grade to the student                                            |
| /lectures/lecture_id  | PATCH  | student_id, professor_id, grad             | 200, update a grade                                                           |
| /stats/credits        | `GET`  | studentId                                  | 200, sum of all credits that the student accumulated through finished courses |
| /stats/grades         | GET    | student_id                                 | 200, list of all grades the student has                                       |
| /stats/grades/history | `GET`  | student_id, lecture_id, startDate, endDate | 200, list of all states of the grade throughout the time period.              |
