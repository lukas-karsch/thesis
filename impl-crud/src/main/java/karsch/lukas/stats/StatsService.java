package karsch.lukas.stats;

import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lectures.*;
import karsch.lukas.users.StudentNotFoundException;
import karsch.lukas.users.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    /**
     * The threshold below which a lecture will be marked as failed
     */
    public static final int FAIL_THRESHOLD = 50;

    private final StudentRepository studentRepository;
    private final AssessmentGradeRepository assessmentGradeRepository;
    private final LectureAssessmentRepository lectureAssessmentRepository;

    public AccumulatedCreditsResponse getAccumulatedCredits(Long studentId) {
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

        int totalCredits = finishedLectures.keySet().stream()
                .map(LectureEntity::getCourse)
                .map(CourseEntity::getCredits)
                .reduce(0, Integer::sum);

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    public AccumulatedCreditsResponse getAccumulatedCreditsImproved(Long studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

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

        int totalCredits = finishedLectures.entrySet().stream()
                .filter(entry -> {
                    int totalAssessments = assessmentsByLecture.getOrDefault(entry.getKey(), Collections.emptyList()).size();
                    int passedAssessments = entry.getValue().size();
                    return totalAssessments > 0 && totalAssessments == passedAssessments;
                })
                .map(entry -> entry.getKey().getCourse().getCredits())
                .reduce(0, Integer::sum);

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    public AccumulatedCreditsResponse getAccumulatedCreditsCustomQuery(Long studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        int totalCredits = assessmentGradeRepository.getAccumulatedCreditsForStudent(student, FAIL_THRESHOLD);

        return new AccumulatedCreditsResponse(studentId, totalCredits);
    }

    /**
     * @return true if the lecture status is FINISHED or ARCHIVED
     */
    private boolean lectureIsFinished(AssessmentGradeEntity grade) {
        LectureStatus lectureStatus = grade.getLectureAssessment().getLecture().getLectureStatus();
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
}
