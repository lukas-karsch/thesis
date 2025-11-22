Took me a while to even realize! The `enrollmentRepository.deleteByStudentIdAndLectureId` method was _NOT_ actually issuing a DELETE statement! Instead it selected the entities first - and apparently, Spring / JPA just decides not to delete the entities.

I am assuming this is due to the bidirectional relationship between lectures, enrollments and waitlist entries. 
## Forcing the query
I can write the queries by hand: 
```java
@Transactional  
@Modifying(clearAutomatically = true)  
@Query("""  
delete from EnrollmentEntity e  
where e.student.id = :studentId  
and e.lecture.id = :lectureId""")  
int deleteByStudentIdAndLectureId(  
        @Param("studentId") Long studentId,  
        @Param("lectureId") Long lectureId  
);
```
This works! But it can mess up my in-memory entity graphs of loaded entities. Because the waitlist and enrollment collection on `LectureEntity` does not not know about the delete.

This is definitely good when doing batch deletes, but can be a little dangerous
## "More correct" in JPA
```java
Lecture lecture = lectureRepository.findById(id).orElseThrow();
lecture.getWaitlist().removeIf(e -> e.getId().equals(waitlistId));
lectureRepository.save(lecture);
```
It is definitely less performant AND you also have to be careful with the entity graph / N+1 queries 
-> "solution" / best approach is this: 
```java
@EntityGraph(attributePaths = {"enrollments", "waitlist"})  
Optional<LectureEntity> findWithEnrollmentsAndWaitlistById(Long id);
```
Which will immediately fetch the collections that will be needed. Of course, this is a little less performant. But it's cleaner JPA and keeps in-memory entity graphs intact. 
## How long did this take me? 
Debugging took me almost all day. When I was writing my E2E tests. And I didn't even realize it when i wrote the test which enrolled and disenrolled a student, because I never did a new `GET` to check the enrollments. 

I realized the problem when I tried to do the automatic enrollment of eligible students when another student disenrolls. 
It took me hours to even figure out _that_ the disenrollment never actually happened. At first, I thought that counting the enrollments in the `enrollStudent` service method did not see the updated value because it happened inside the same transaction - this was of course confirmed by ChatGPT, leading me on a wrong route when debugging. 

I figured it out when I looked at the SQL logs and saw that no delete statement was ever issued.
### Learnings 
Performance is desirable (which is why I avoided working directly on the lecture entity, and instead tried fetching data from other repositories). **BUT** when working with data in complex relationships, JPA can be very intransparent about what it is doing. Querying the "root" entity ( #ddd ) and only changing it by modifying its associations, is actually way safer here. 