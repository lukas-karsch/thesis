I tried running k6 test `enroll-to-lecture.js`
when debugging another issue, i found out that the CollectionWithIdSerializer is insanely slow 
N+1 query is not at all avoided by my custom serialization approach

The bug in `enroll-to-lecture.js` must also still be solved 
## Solution
The only solution seems to be to switch to Envers. I will do that now, every entity is @Audited 
## Implementing historic query for grades using envers 
Code example from Gemini, no clue if this will work:

---
To implement this, you’ll use the `AuditReader`, which is the Envers equivalent of the `EntityManager`.
### 1. Setup the Repository/Service Logic

In Envers, you query for "revisions." For a grade history, you want all revisions of a specific `AssessmentGradeEntity` between two dates.

```java
public GradeHistoryResponse getGradeHistoryForAssessment(UUID studentId, UUID lectureAssessmentId, LocalDateTime startDate, LocalDateTime endDate) {
    // 1. Find the current grade ID (same as your current logic)
    var grade = assessmentGradeRepository.findByStudentAndLectureAssessment(...)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // 2. Initialize the AuditReader
    AuditReader reader = AuditReaderFactory.get(entityManager);

    // 3. Create the Query
    // We want versions of the GradeEntity where the ID matches
    AuditQuery query = reader.createQuery()
            .forRevisionsOfEntity(AssessmentGradeEntity.class, false, true)
            .add(AuditEntity.id().eq(grade.getId()));

    // 4. Add Date Filters
    // Note: Envers uses Revision Timestamps. We filter by the associated Revision Entity.
    if (startDate != null) {
        query.add(AuditEntity.revisionProperty("timestamp").ge(startDate.toEpochSecond(ZoneOffset.UTC)));
    }
    if (endDate != null) {
        query.add(AuditEntity.revisionProperty("timestamp").le(endDate.toEpochSecond(ZoneOffset.UTC)));
    }

    // 5. Execute and Map
    List<Object[]> results = query.getResultList();
    
    var gradeChanges = results.stream()
            .map(result -> {
                AssessmentGradeEntity entity = (AssessmentGradeEntity) result[0];
                DefaultRevisionEntity revision = (DefaultRevisionEntity) result[1];
                RevisionType type = (RevisionType) result[2]; // ADD, MOD, DEL

                return new GradeChangeDTO(
                        lectureAssessmentId,
                        entity.getGradeValue(), // No JSON parsing needed!
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(revision.getTimestamp()), ZoneId.systemDefault())
                );
            })
            .toList();

    return new GradeHistoryResponse(studentId, lectureAssessmentId, gradeChanges);
}
```

---

### 2. Why this is "Cleaner" than your current setup

1. **Type Safety:** Notice `entity.getGradeValue()`. Since Envers reconstructs the actual Java object for that point in time, you don't need a `ObjectMapper` or custom string extraction logic.
2. **Performance:** Envers queries are indexed SQL queries against a structured table (`ASSESSMENT_GRADE_AUD`). Your current `AuditService` is scanning a generic log table and then doing expensive JSON parsing in-memory.
3. **Audit Context:** If you want to know _who_ changed the grade, you can extend `DefaultRevisionEntity` to include a `userId`. Envers will populate this automatically during the transaction.
### 3. Key Differences in Implementation
- **The "Triple" Result:** When you use `forRevisionsOfEntity(..., false, true)`, Envers returns a `List<Object[]>` where each array contains:
    1. The Entity state at that time.
    2. The Revision metadata (timestamp, ID, user).
    3. The Type of change (Created, Modified, or Deleted).
- **The ID Trap:** In your current code, you used `entityManager.getReference`. In Envers, if you are querying the history of a relation, Envers prefers the actual ID or the reconstructed proxy.
### 4. Important Gotcha: The `timestamp`

By default, Hibernate Envers stores timestamps as a `long` (milliseconds) in the `REVINFO` table. In my example above, I converted your `LocalDateTime` to a timestamp. If you use a custom Revision Entity with a proper `LocalDateTime`, you can query that property directly.

---
### What’s next for you?
Since you already have an `AuditLogRepository`, you'll need to decide if you're going to migrate your old logs or just start fresh with Envers.

**Would you like me to show you how to customize the Revision Entity so you can store the `professorId` automatically whenever a grade is changed?**