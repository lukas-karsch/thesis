I outlined the basic structure, but I think I need some more business logic. DDD makes sense because of it is closely modeled to a complex domain, so business rules are important - otherwise I can not enforce any invariants and logic! Also, linked relationships and large aggregates are necessary. So, let's keep brainstorming.
## Ideas for additional business logic
### Lectures
- Add a lifecycle: `OpenForEnrollment` $\rightarrow$ `InProgress` $\rightarrow$ `Finished` $\rightarrow$ `Archived`
### Enrollment
- Higher-semester students have an advantage enrolling if the lecture is already full
- Some courses or a specific amount of credits can be prerequisite for enrolling
- As soon as a lecture is full, students who try to enroll are put on a _waiting list_
- Can not enroll for lectures with overlapping dates
- Enrollment only active during `OpenForEnrollment`
### Grades / Credits
- Final grade for a lecture is a composite of several `assessments`: e.g. 30% exam, 70% project
	- might need a sort of `LectureAssessment` with a (due) date or exam date 
- Only professors teaching the lecture may change grades
- Grades can only be assigned during `InProgress` or `Finished`
- Credits are calculated automatically when the lecture lifecycle is set to `Finished` and all grades on the lecture assignments are "Passed" (grade >=50)
## Resulting changes to the domain objects / entities
### Course
Additions:
- `prerequisites`: list of courses that must be finished before enrolling
### Lecture
- make sure `dates` is an array of value objects that include startTime and endTime
- `status` field with a lifecycle
- `waitingList`
### Grade
- `isFinal`
- `assessments` field -> map of assessment to weighting
    - total weighting must equal 1
### (+) Assessment
- `assessmentType`: e.g. exam, project, paper
- `grade` 
## Resulting changes to endpoints 
### Lecture 
(+) `POST` /lectures/{lecture_id}/dates
Add dates to a lecture (for professors)

(+) `POST` /lectures/{lecture_id}/lifecycle
Advance the life cycle of a lecture (for professors)

(+) `GET` /lectures/{lecture_id}/waitingList
Get the waitlist of a lecture 

(+) `POST` /lectures/{lecture_id}/assessments 
Add an assessment to a lecture (for professors)
