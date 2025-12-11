package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.transaction.Transactional;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.lecture.GetLecturesForStudentResponse;
import karsch.lukas.lecture.LectureDTO;
import karsch.lukas.lecture.LectureDetailDTO;
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

import java.util.List;

@Component
@Slf4j
@ProcessingGroup("lectures")
@RequiredArgsConstructor
public class StudentLecturesProjector {

    private final QueryGateway queryGateway;
    private final ObjectMapper objectMapper;
    private final StudentLecturesRepository studentLecturesRepository;

    @EventHandler
    @Transactional
    @Retryable
    public void on(StudentEnrolledEvent e) throws JsonProcessingException {
        logRetries();

        var lecture = queryGateway.query(new FindLectureByIdQuery(e.lectureId()), ResponseTypes.instanceOf(LectureDetailDTO.class)).join();
        if (lecture == null) {
            throw new IllegalStateException("Lecture not found in repository");
        }

        var entity = studentLecturesRepository.findById(e.studentId())
                .orElseGet(() -> {
                    var newEntity = new StudentLecturesProjectionEntity();
                    newEntity.setId(e.studentId());
                    return newEntity;
                });

        List<LectureDTO> enrolled = objectMapper.readerForListOf(LectureDTO.class).readValue(entity.getEnrolledJson());
        enrolled.add(new LectureDTO(
                lecture.lectureId(),
                new SimpleCourseDTO(lecture.course().id(), lecture.course().name(), lecture.course().description(), lecture.course().credits()),
                lecture.maximumStudents(),
                lecture.dates(),
                lecture.professor(),
                lecture.status()
        ));
        entity.setEnrolledJson(objectMapper.writeValueAsString(enrolled));
        entity.getEnrolledIds().add(e.lectureId());

        List<LectureDTO> waitlisted = objectMapper.readerForListOf(LectureDTO.class).readValue(entity.getWaitlistedJson());
        waitlisted.removeIf(l -> l.id().equals(e.lectureId()));
        entity.setWaitlistedJson(objectMapper.writeValueAsString(waitlisted));
        entity.getWaitlistedIds().remove(e.lectureId());

        studentLecturesRepository.save(entity);
    }

    @EventHandler
    @Transactional
    @Retryable
    public void on(StudentWaitlistedEvent e) throws JsonProcessingException {
        logRetries();

        var lecture = queryGateway.query(new FindLectureByIdQuery(e.lectureId()), ResponseTypes.instanceOf(LectureDetailDTO.class)).join();
        if (lecture == null) {
            throw new IllegalStateException("Lecture not found in repository");
        }

        var entity = studentLecturesRepository.findById(e.studentId())
                .orElseGet(() -> {
                    var newEntity = new StudentLecturesProjectionEntity();
                    newEntity.setId(e.studentId());
                    return newEntity;
                });

        List<LectureDTO> waitlisted = objectMapper.readerForListOf(LectureDTO.class).readValue(entity.getWaitlistedJson());
        waitlisted.add(new LectureDTO(
                lecture.lectureId(),
                new SimpleCourseDTO(lecture.course().id(), lecture.course().name(), lecture.course().description(), lecture.course().credits()),
                lecture.maximumStudents(),
                lecture.dates(),
                lecture.professor(),
                lecture.status()
        ));
        entity.getWaitlistedIds().add(e.lectureId());

        entity.setWaitlistedJson(objectMapper.writeValueAsString(waitlisted));
        studentLecturesRepository.save(entity);
    }

    @EventHandler
    @Transactional
    @Retryable
    public void on(WaitlistClearedEvent event) {
        logRetries();
        var affectedStudents = studentLecturesRepository.findByLectureIdInWaitlist(event.lectureId());
        affectedStudents
                .forEach(s -> {
                    s.getWaitlistedIds().remove(event.lectureId());
                    try {
                        List<LectureDTO> waitlist = objectMapper.readerForListOf(LectureDTO.class).readValue(s.getWaitlistedJson());
                        waitlist.removeIf(l -> l.id().equals(event.lectureId()));
                        s.setWaitlistedJson(objectMapper.writeValueAsString(waitlist));
                        log.debug("removed lecture {} from student {} waitlist.", event.lectureId(), s.getId());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
        studentLecturesRepository.saveAll(affectedStudents);
    }

    @QueryHandler
    public GetLecturesForStudentResponse getLecturesForStudent(GetLecturesForStudentQuery query) throws JsonProcessingException {
        var entity = studentLecturesRepository.findById(query.studentId()).orElseThrow();

        final ObjectReader reader = objectMapper.readerForListOf(LectureDTO.class);
        return new GetLecturesForStudentResponse(
                reader.readValue(entity.getEnrolledJson()),
                reader.readValue(entity.getWaitlistedJson())
        );
    }

    private static void logRetries() {
        // TODO create an aspect that does this for every method annotated with @Retryable
        if (RetrySynchronizationManager.getContext().getRetryCount() > 0) {
            log.debug("Retry #{}", RetrySynchronizationManager.getContext().getRetryCount());
        }
    }

}
