package karsch.lukas.features.stats.queries.grades;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.enrollment.api.GradeAssignedEvent;
import karsch.lukas.features.enrollment.api.GradeUpdatedEvent;
import karsch.lukas.features.lectures.api.AssessmentAddedEvent;
import karsch.lukas.features.lectures.api.LectureCreatedEvent;
import karsch.lukas.features.stats.api.GetGradesForStudentQuery;
import karsch.lukas.lecture.SimpleLectureDTO;
import karsch.lukas.stats.GradeDTO;
import karsch.lukas.stats.GradedAssessmentDTO;
import karsch.lukas.stats.GradesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("grades")
class StudentGradesProjector {

    private final StudentGradesRepository studentGradesRepository;
    private final AssessmentProjectionRepository assessmentProjectionRepository;
    private final SimpleCourseProjectionRepository simpleCourseProjectionRepository;
    private final SimpleLectureProjectionRepository simpleLectureProjectionRepository;
    private final GradedAssessmentProjectionRepository gradedAssessmentProjectionRepository;

    private final ObjectMapper objectMapper;

    @EventHandler
    public void on(AssessmentAddedEvent event) {
        var entity = new AssessmentProjectionEntity(event.assessmentId(), event.lectureId(), event.assessmentType(), event.weight());
        assessmentProjectionRepository.save(entity);
    }

    @EventHandler
    public void on(CourseCreatedEvent event) {
        var entity = new SimpleCourseProjectionEntity(event.courseId(), event.name(), event.credits());
        simpleCourseProjectionRepository.save(entity);
    }

    @EventHandler
    @Retryable(retryFor = IllegalStateException.class)
    public void on(LectureCreatedEvent event) {
        var course = simpleCourseProjectionRepository.findById(event.courseId())
                .orElseThrow(() -> new IllegalStateException("Course not found in SimpleCourseProjectionRepository"));
        var entity = new SimpleLectureProjectionEntity(event.lectureId(), course.getId(), course.getName());
        simpleLectureProjectionRepository.save(entity);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = IllegalStateException.class)
    public void on(GradeAssignedEvent gradeAssignedEvent) throws JsonProcessingException {
        // record the grade itself
        AssessmentProjectionEntity assessment = getAssessmentById(gradeAssignedEvent.assessmentId());

        boolean gradeIsAlreadyRecorded = studentGradesRepository.existsById(
                new StudentGradesProjectionEntityId(gradeAssignedEvent.studentId(), assessment.getLectureId())
        );
        if (gradeIsAlreadyRecorded) {
            return;
        }

        var entity = new GradedAssessmentProjectionEntity(
                assessment.getId(),
                gradeAssignedEvent.studentId(),
                assessment.getAssessmentType(),
                gradeAssignedEvent.grade(),
                assessment.getWeight(),
                gradeAssignedEvent.assignedAt(),
                assessment.getLectureId()
        );
        gradedAssessmentProjectionRepository.save(entity);

        Long assessmentsForLectureCount = assessmentProjectionRepository
                .countByLectureId(assessment.getLectureId());

        List<GradedAssessmentProjectionEntity> gradesForLecture = gradedAssessmentProjectionRepository
                .findByLectureIdAndStudentId(assessment.getLectureId(), gradeAssignedEvent.studentId());

        var lecture = simpleLectureProjectionRepository.findById(assessment.getLectureId())
                .orElseThrow(() -> new IllegalStateException("Lecture not found in LectureProjectionRepository"));

        var course = simpleCourseProjectionRepository.findById(lecture.getCourseId())
                .orElseThrow(() -> new IllegalStateException("Course not found in SimpleCourseProjectionRepository"));

        // now, update the GradeDto
        int combinedGrade = calculateCombinedGrade(gradesForLecture);
        boolean lectureIsFailed = combinedGrade < 50;
        var newGradeDto = new GradeDTO(
                lectureIsFailed ? 0 : combinedGrade,
                lectureIsFailed ? 0 : course.getCredits(),
                new SimpleLectureDTO(lecture.getId(), lecture.getCourseId(), course.getName()),
                map(gradesForLecture),
                gradesForLecture.size() == assessmentsForLectureCount,
                lectureIsFailed
        );
        var newGradesProjectionEntity = new StudentGradesProjectionEntity(
                gradeAssignedEvent.studentId(),
                assessment.getLectureId(),
                objectMapper.writeValueAsString(newGradeDto)
        );
        studentGradesRepository.save(newGradesProjectionEntity);
    }

    private AssessmentProjectionEntity getAssessmentById(UUID assessmentId) {
        return assessmentProjectionRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalStateException("Assessment not found in AssessmentProjectionRepository"));
    }

    private int calculateCombinedGrade(List<GradedAssessmentProjectionEntity> grades) {
        if (grades.isEmpty()) {
            return 0;
        }

        float totalGrades = grades.stream()
                .map(g -> g.getGrade() * g.getWeight())
                .reduce(0f, Float::sum);

        float totalWeight = grades.stream()
                .map(GradedAssessmentProjectionEntity::getWeight)
                .reduce(0f, Float::sum);

        return Math.round(totalGrades / totalWeight);
    }

    private List<GradedAssessmentDTO> map(List<GradedAssessmentProjectionEntity> gradedAssessmentProjectionEntities) {
        return gradedAssessmentProjectionEntities
                .stream()
                .map(e -> new GradedAssessmentDTO(
                        e.getAssessmentId(),
                        e.getAssessmentType(),
                        e.getGrade(),
                        e.getWeight(),
                        e.getAssignedAt()
                ))
                .toList();
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = IllegalStateException.class)
    public void on(GradeUpdatedEvent gradeUpdatedEvent) throws JsonProcessingException {
        var assessment = getAssessmentById(gradeUpdatedEvent.assessmentId());

        var gradedAssessmentEntity = gradedAssessmentProjectionRepository
                .findById(new GradedAssessmentId(assessment.getId(), gradeUpdatedEvent.studentId()))
                .orElseThrow();

        gradedAssessmentEntity.setGrade(gradeUpdatedEvent.grade());
        gradedAssessmentProjectionRepository.save(gradedAssessmentEntity);

        var studentGrade = studentGradesRepository.findById(new StudentGradesProjectionEntityId(gradeUpdatedEvent.studentId(), assessment.getLectureId()))
                .orElseThrow();

        GradeDTO gradeDTO = objectMapper.readerFor(GradeDTO.class).readValue(studentGrade.getGradeDtoJson());

        // recalculate credits and build new DTO
        long assessmentsForLectureCount = assessmentProjectionRepository
                .countByLectureId(assessment.getLectureId());

        List<GradedAssessmentProjectionEntity> gradesForLecture = gradedAssessmentProjectionRepository
                .findByLectureIdAndStudentId(assessment.getLectureId(), gradeUpdatedEvent.studentId());

        int combinedGrade = calculateCombinedGrade(gradesForLecture);
        boolean lectureIsFailed = combinedGrade < 50;
        var newGradeDto = new GradeDTO(
                lectureIsFailed ? 0 : combinedGrade,
                lectureIsFailed ? 0 : gradeDTO.credits(),
                gradeDTO.lecture(),
                map(gradesForLecture),
                gradesForLecture.size() == assessmentsForLectureCount,
                lectureIsFailed
        );
        studentGrade.setGradeDtoJson(objectMapper.writeValueAsString(newGradeDto));
        studentGradesRepository.save(studentGrade);
    }

    @QueryHandler
    public GradesResponse getGradesForStudent(GetGradesForStudentQuery query) throws JsonProcessingException {
        List<StudentGradesProjectionEntity> grades = studentGradesRepository.findByStudentId(query.studentId());

        List<GradeDTO> gradeDTOS = new ArrayList<>();
        for (var grade : grades) {
            gradeDTOS.add(objectMapper.readerFor(GradeDTO.class).readValue(grade.getGradeDtoJson()));
        }

        return new GradesResponse(
                query.studentId(),
                gradeDTOS
        );
    }

}
