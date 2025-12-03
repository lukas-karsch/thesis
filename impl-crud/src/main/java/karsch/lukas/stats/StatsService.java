package karsch.lukas.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import karsch.lukas.audit.AuditLogEntry;
import karsch.lukas.audit.AuditService;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lectures.*;
import karsch.lukas.users.StudentEntity;
import karsch.lukas.users.StudentNotFoundException;
import karsch.lukas.users.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    /**
     * The threshold below which a lecture will be marked as failed
     */
    public static final int FAIL_THRESHOLD = 50;

    private final AuditService auditService;

    private final StudentRepository studentRepository;
    private final AssessmentGradeRepository assessmentGradeRepository;
    private final LectureAssessmentRepository lectureAssessmentRepository;
    private final SimpleLectureDtoMapper simpleLectureDtoMapper;
    private final GradedAssessmentDtoMapper gradedAssessmentDtoMapper;

    private final EntityManager entityManager;

    public AccumulatedCreditsResponse getAccumulatedCredits(UUID studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        var allGrades = assessmentGradeRepository.findAllByStudent(student);

        var finishedLectures = allGrades.stream()
                .filter(this::hasPassed)
                .filter(this::lectureIsFinished)
                .collect(Collectors.groupingBy(a -> a.getLectureAssessment().getLecture()))
                .entrySet()
                .stream()
                .filter(entry -> hasCompletedAllAssessmentsOfCourse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int totalCredits = countCreditsFromLectures(finishedLectures.keySet().stream());

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    public AccumulatedCreditsResponse getAccumulatedCreditsImproved(UUID studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        int totalCredits = countCreditsFromLectures(getPassedLectures(student));

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    public int countCreditsFromLectures(Stream<LectureEntity> lectures) {
        return countCreditsFromCourses(lectures.map(LectureEntity::getCourse));
    }

    public int countCreditsFromCourses(Stream<CourseEntity> courses) {
        return courses
                .map(CourseEntity::getCredits)
                .reduce(0, Integer::sum);
    }

    public Stream<LectureEntity> getPassedLectures(StudentEntity student) {
        // 1. Eagerly fetch grades with lectures and courses in one query.
        var allGrades = assessmentGradeRepository.findAllByStudent(student);

        // 2. Get the unique lectures.
        Set<LectureEntity> relevantLectures = allGrades.stream()
                .map(grade -> grade.getLectureAssessment().getLecture())
                .collect(Collectors.toSet());

        // 3. Fetch all assessments for those lectures in a second query.
        Map<LectureEntity, List<LectureAssessmentEntity>> assessmentsByLecture =
                lectureAssessmentRepository.findAllByLectureIn(relevantLectures).stream()
                        .collect(Collectors.groupingBy(LectureAssessmentEntity::getLecture));

        // 4. Now, process everything in-memory without causing more queries.
        var finishedLectures = allGrades.stream()
                .filter(this::hasPassed)
                .filter(this::lectureIsFinished)
                .collect(Collectors.groupingBy(a -> a.getLectureAssessment().getLecture()));

        return finishedLectures.entrySet().stream()
                .filter(entry -> {
                    int totalLectureAssessments = assessmentsByLecture.getOrDefault(entry.getKey(), Collections.emptyList()).size();
                    int passedAssessments = entry.getValue().size();
                    return totalLectureAssessments > 0 && totalLectureAssessments == passedAssessments;
                })
                .map(Map.Entry::getKey);
    }

    public AccumulatedCreditsResponse getAccumulatedCreditsCustomQuery(UUID studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        int totalCredits = assessmentGradeRepository.getAccumulatedCreditsForStudent(student, FAIL_THRESHOLD);

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    /**
     * @return true if the lecture status is FINISHED or ARCHIVED
     */
    private boolean lectureIsFinished(AssessmentGradeEntity grade) {
        final LectureEntity lecture = grade.getLectureAssessment().getLecture();
        final LectureStatus lectureStatus = lecture.getLectureStatus();
        log.debug("Lecture {} status: {}", lecture.getId(), lectureStatus);
        return lectureStatus.ordinal() >= LectureStatus.FINISHED.ordinal();
    }

    /**
     * @return true if the grade is >= the fail threshold
     */
    private boolean hasPassed(AssessmentGradeEntity grade) {
        return grade.getGrade() >= FAIL_THRESHOLD;
    }

    private boolean hasCompletedAllAssessmentsOfCourse(LectureEntity lecture, List<AssessmentGradeEntity> grades) {
        return lecture.getAssessments().size() == grades.size();
    }

    public GradesResponse getGradesForStudent(UUID studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        var allGrades = assessmentGradeRepository.findAllByStudent(student);

        Map<LectureEntity, List<AssessmentGradeEntity>> allGradesByLecture = allGrades.stream()
                .collect(Collectors.groupingBy(a -> a.getLectureAssessment().getLecture()));

        // 3. Fetch all assessments for those lectures in a second query.
        Map<LectureEntity, List<LectureAssessmentEntity>> allAssessmentsByLecture =
                lectureAssessmentRepository.findAllByLectureIn(allGradesByLecture.keySet()).stream()
                        .collect(Collectors.groupingBy(LectureAssessmentEntity::getLecture));

        var grades = allGradesByLecture.entrySet()
                .stream()
                .map(entry -> {
                    int combinedGrade = getCombinedGrade(entry.getValue());
                    boolean failed = combinedGrade < FAIL_THRESHOLD;
                    int credits = failed ? 0 : entry.getKey().getCourse().getCredits();

                    int totalAssessments = allAssessmentsByLecture.getOrDefault(entry.getKey(), Collections.emptyList()).size();
                    int passedAssessments = entry.getValue().size();
                    boolean isFinalGrade = totalAssessments > 0 && totalAssessments == passedAssessments;

                    return new GradeDTO(
                            combinedGrade,
                            credits,
                            simpleLectureDtoMapper.map(entry.getKey()),
                            gradedAssessmentDtoMapper.mapToList(entry.getValue()),
                            isFinalGrade,
                            failed);
                })
                .collect(Collectors.toList());

        return new GradesResponse(studentId, grades);
    }

    private int getCombinedGrade(List<AssessmentGradeEntity> grades) {
        if (grades.isEmpty()) {
            return 0;
        }

        float totalGrades = grades.stream()
                .map(g -> g.getGrade() * g.getLectureAssessment().getWeight())
                .reduce(0f, Float::sum);

        float totalWeight = grades.stream()
                .map(g -> g.getLectureAssessment().getWeight())
                .reduce(0f, Float::sum);

        return Math.round(totalGrades / totalWeight);
    }

    public GradeHistoryResponse getGradeHistoryForAssessment(UUID studentId, long lectureAssessmentId, LocalDateTime startDate, LocalDateTime endDate) {
        var assessment = lectureAssessmentRepository.findById(lectureAssessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var grade = assessmentGradeRepository.findByStudentAndLectureAssessment(
                entityManager.getReference(StudentEntity.class, studentId),
                assessment
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var auditLogEntries = auditService.getByEntityId(grade.getClass(), grade.getId(), startDate, endDate);
        log.debug("Found {}, auditLogEntries for grade {}: {}", auditLogEntries.size(), grade.getId(), auditLogEntries);

        final ObjectMapper mapper = new ObjectMapper();
        var gradeChanges = auditLogEntries.stream()
                .map(entry -> new GradeChangeDTO(
                        assessment.getId(),
                        extractGradeFromAuditEntry(entry, mapper),
                        entry.getTimestamp())
                )
                .toList();

        return new GradeHistoryResponse(
                studentId,
                lectureAssessmentId,
                gradeChanges
        );
    }

    private int extractGradeFromAuditEntry(AuditLogEntry auditLogEntry, ObjectMapper mapper) {
        String newJson = auditLogEntry.getNewValueJson();
        if (newJson == null) {
            throw new RuntimeException("newJson is null null on audit log entry " + auditLogEntry.getId());
        }
        try {
            return mapper
                    .readTree(newJson)
                    .get("grade")
                    .asInt();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
