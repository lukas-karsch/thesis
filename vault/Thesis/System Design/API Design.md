As discussed in [[2025-11-09 API Design (2f6e551d)]], this will be the endpoints: 

| endpoint              | method | query parameters                           | returns                                                                       |
| --------------------- | ------ | ------------------------------------------ | ----------------------------------------------------------------------------- |
| /lectures/enroll      | `POST` | studentId                                  | 201, `{success: true}`                                                        |
| /stats/credits        | `GET`  | studentId                                  | 200, sum of all credits that the student accumulated through finished courses |
| /stats/grades/history | `GET`  | student_id, lecture_id, startDate, endDate | 200, list of all states of the grade throughout the time period.              |
