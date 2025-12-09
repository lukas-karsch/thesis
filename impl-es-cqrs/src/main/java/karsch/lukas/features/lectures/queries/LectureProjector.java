package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.professor.api.FindProfessorByIdQuery;
import karsch.lukas.lecture.LectureAssessmentDTO;
import karsch.lukas.lecture.LectureDetailDTO;
import karsch.lukas.lecture.SimpleLectureDTO;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.professor.ProfessorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@Slf4j
@ProcessingGroup("lectures")
@RequiredArgsConstructor
public class LectureProjector {

    private final LectureRepository lectureRepository;
    private final QueryGateway queryGateway;
    private final ObjectMapper objectMapper;

    @EventHandler
    @Retryable(retryFor = {IllegalStateException.class, CompletionException.class})
    public void on(LectureCreatedEvent event) throws JsonProcessingException {
        var courseFuture = queryGateway.query(
                new FindCourseByIdQuery(event.courseId()),
                ResponseTypes.instanceOf(CourseDTO.class)
        );

        var professorFuture = queryGateway.query(
                new FindProfessorByIdQuery(event.professorId()),
                ResponseTypes.instanceOf(ProfessorDTO.class)
        );

        CompletableFuture.allOf(courseFuture, professorFuture).join();

        var course = courseFuture.join();
        var professor = professorFuture.join();

        if (course == null) {
            throw new IllegalStateException("Course not found for ID: " + event.courseId());
        }
        if (professor == null) {
            throw new IllegalStateException("Professor not found for ID: " + event.professorId());
        }

        var lectureEntity = new LectureProjectionEntity();
        lectureEntity.setId(event.lectureId());
        lectureEntity.setCourseId(event.courseId());
        lectureEntity.setLectureStatus(event.lectureStatus());
        lectureEntity.setProfessorId(event.professorId());

        // JSON processing (denormalized entity)
        lectureEntity.setCourseDtoJson(objectMapper.writeValueAsString(course));
        lectureEntity.setProfessorDtoJson(objectMapper.writeValueAsString(professor));
        lectureEntity.setDatesJson(objectMapper.writeValueAsString(event.dates()));
        lectureEntity.setAssessmentsJson("[]");

        log.debug("Projected lecture {}", event.lectureId());
        lectureRepository.save(lectureEntity);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(LectureLifecycleAdvancedEvent event) {
        logRetries();
        var lecture = lectureRepository.findById(event.lectureId()).orElseThrow();
        lecture.setLectureStatus(event.lectureStatus());
        lectureRepository.save(lecture);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(AssessmentAddedEvent event) throws JsonProcessingException {
        logRetries();
        var lecture = lectureRepository.findById(event.lectureId()).orElseThrow();
        List<LectureAssessmentDTO> assessments = objectMapper.readerForListOf(LectureAssessmentDTO.class).readValue(lecture.getAssessmentsJson());
        assessments.add(
                new LectureAssessmentDTO(
                        new SimpleLectureDTO(lecture.getId(), lecture.getCourseId(), objectMapper.readValue(lecture.getCourseDtoJson(), CourseDTO.class).name()),
                        event.assessmentType(),
                        event.timeSlot(),
                        event.weight()
                )
        );
        lecture.setAssessmentsJson(objectMapper.writeValueAsString(assessments));
        lectureRepository.save(lecture);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(TimeSlotsAssignedEvent event) throws JsonProcessingException {
        logRetries();
        var lecture = lectureRepository.findById(event.lectureId()).orElseThrow();
        List<TimeSlot> timeSlots = objectMapper.readerForListOf(TimeSlot.class).readValue(lecture.getDatesJson());
        timeSlots.addAll(event.newTimeSlots());
        lecture.setDatesJson(objectMapper.writeValueAsString(timeSlots));
        lectureRepository.save(lecture);
    }

    @QueryHandler
    public LectureDetailDTO findById(FindLectureByIdQuery query) throws JsonProcessingException {
        var lecture = lectureRepository.findById(query.lectureId()).orElse(null);

        if (lecture == null) {
            return null;
        }

        return new LectureDetailDTO(
                lecture.getId(),
                objectMapper.readValue(lecture.getCourseDtoJson(), CourseDTO.class),
                lecture.getMaximumStudents(),
                objectMapper.readerForListOf(TimeSlot.class).readValue(lecture.getDatesJson()),
                objectMapper.readValue(lecture.getProfessorDtoJson(), ProfessorDTO.class),
                Collections.emptySet(), // TODO
                Collections.emptyList(), // TODO
                lecture.getLectureStatus(),
                new HashSet<>(objectMapper.readerForListOf(LectureAssessmentDTO.class).readValue(lecture.getAssessmentsJson()))
        );
    }

    private static void logRetries() {
        // TODO create an aspect that does this for every method annotated with @Retryable
        if (RetrySynchronizationManager.getContext().getRetryCount() > 0) {
            log.debug("Retry #{}", RetrySynchronizationManager.getContext().getRetryCount());
        }
    }

}
