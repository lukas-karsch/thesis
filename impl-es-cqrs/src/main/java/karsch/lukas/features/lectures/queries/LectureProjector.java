package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.lectures.exceptions.LectureNotFoundException;
import karsch.lukas.features.student.api.FindStudentByIdQuery;
import karsch.lukas.lecture.*;
import karsch.lukas.professor.ProfessorDTO;
import karsch.lukas.student.StudentDTO;
import karsch.lukas.time.TimeSlotComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionException;

import static karsch.lukas.core.json.Defaults.EMPTY_LIST;

@Component
@Slf4j
@ProcessingGroup("lectures")
@RequiredArgsConstructor
public class LectureProjector {

    private final LectureDetailRepository lectureDetailRepository;
    private final QueryGateway queryGateway;
    private final ObjectMapper objectMapper;
    private final QueryUpdateEmitter updateEmitter;
    private final ProfessorRepository professorRepository;

    @EventHandler
    @Retryable(retryFor = {IllegalStateException.class, CompletionException.class})
    public void on(LectureCreatedEvent event) throws JsonProcessingException {
        var courseFuture = queryGateway.query( // TODO Don't use queryGateway here
                new FindCourseByIdQuery(event.courseId()),
                ResponseTypes.instanceOf(CourseDTO.class)
        );

        var professor = professorRepository.findById(event.professorId())
                .orElseThrow(() -> new IllegalStateException("Professor " + event.professorId() + " not found"));

        var course = courseFuture.join();
        if (course == null) {
            throw new IllegalStateException("Course not found for ID: " + event.courseId());
        }

        var lectureEntity = new LectureDetailProjectionEntity();
        lectureEntity.setId(event.lectureId());
        lectureEntity.setCourseId(event.courseId());
        lectureEntity.setLectureStatus(event.lectureStatus());
        lectureEntity.setProfessorId(event.professorId());

        // JSON processing (denormalized entity)
        lectureEntity.setCourseDtoJson(objectMapper.writeValueAsString(course));
        lectureEntity.setProfessorDtoJson(objectMapper.writeValueAsString(professor));
        lectureEntity.setDatesJson(objectMapper.writeValueAsString(event.dates()));
        lectureEntity.setAssessmentsJson(EMPTY_LIST);
        lectureEntity.setEnrolledStudentsDtoJson(EMPTY_LIST);
        lectureEntity.setWaitingListDtoJson(EMPTY_LIST);

        log.debug("Projected lecture {}", event.lectureId());
        lectureDetailRepository.save(lectureEntity);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(LectureLifecycleAdvancedEvent event) {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();
        lecture.setLectureStatus(event.lectureStatus());
        lectureDetailRepository.save(lecture);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(AssessmentAddedEvent event) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();
        List<LectureAssessmentDTO> assessments = objectMapper.readerForListOf(LectureAssessmentDTO.class).readValue(lecture.getAssessmentsJson());
        assessments.add(
                new LectureAssessmentDTO(
                        event.assessmentType(),
                        event.timeSlot(),
                        event.weight()
                )
        );
        lecture.setAssessmentsJson(objectMapper.writeValueAsString(assessments));
        lectureDetailRepository.save(lecture);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(TimeSlotsAssignedEvent event) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();
        List<TimeSlot> timeSlots = objectMapper.readerForListOf(TimeSlot.class).readValue(lecture.getDatesJson());
        timeSlots.addAll(event.newTimeSlots());
        timeSlots.sort(new TimeSlotComparator());
        lecture.setDatesJson(objectMapper.writeValueAsString(timeSlots));
        lectureDetailRepository.save(lecture);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class, IllegalStateException.class})
    public void on(StudentEnrolledEvent event) throws JsonProcessingException {
        var studentFuture = queryGateway.query(new FindStudentByIdQuery(event.studentId()), ResponseTypes.instanceOf(StudentDTO.class));

        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<StudentDTO> enrolledStudents = objectMapper.readerForListOf(StudentDTO.class).readValue(lecture.getEnrolledStudentsDtoJson());

        var student = studentFuture.join();
        if (student == null) {
            throw new IllegalStateException("Student not found for ID: " + event.studentId());
        }

        enrolledStudents.add(student);

        List<WaitlistedStudentDTO> waitlist = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());
        waitlist.removeIf(w -> w.student().id().equals(event.studentId()));
        lecture.setWaitingListDtoJson(objectMapper.writeValueAsString(waitlist));

        lecture.setEnrolledStudentsDtoJson(objectMapper.writeValueAsString(enrolledStudents));
        lectureDetailRepository.save(lecture);

        updateEmitter.emit(EnrollmentStatusQuery.class,
                q -> q.lectureId().equals(event.lectureId()) && q.studentId().equals(event.studentId()),
                new EnrollmentStatusUpdate(event.lectureId(), event.studentId(), EnrollmentStatus.ENROLLED)
        );
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {NoSuchElementException.class, IllegalStateException.class})
    public void on(StudentWaitlistedEvent event) throws JsonProcessingException {
        var studentFuture = queryGateway.query(new FindStudentByIdQuery(event.studentId()), ResponseTypes.instanceOf(StudentDTO.class));

        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<WaitlistedStudentDTO> waitlist = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());

        var student = studentFuture.join();
        if (student == null) {
            throw new IllegalStateException("Student not found for ID: " + event.studentId());
        }

        waitlist.add(
                new WaitlistedStudentDTO(
                        student,
                        event.timestamp().atZone(ZoneId.of("UTC")).toLocalDateTime()
                )
        );

        lecture.setWaitingListDtoJson(objectMapper.writeValueAsString(waitlist));
        lectureDetailRepository.save(lecture);

        updateEmitter.emit(EnrollmentStatusQuery.class,
                q -> q.lectureId().equals(event.lectureId()) && q.studentId().equals(event.studentId()),
                new EnrollmentStatusUpdate(event.lectureId(), event.studentId(), EnrollmentStatus.WAITLISTED)
        );
    }

    @Transactional
    @EventHandler
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(WaitlistClearedEvent event) {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        lecture.setWaitingListDtoJson(EMPTY_LIST);
        lectureDetailRepository.save(lecture);
    }

    @Transactional
    @EventHandler
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(StudentRemovedFromWaitlistEvent event) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<WaitlistedStudentDTO> waitlist = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());
        waitlist.removeIf(w -> w.student().id().equals(event.studentId()));
        lecture.setWaitingListDtoJson(objectMapper.writeValueAsString(waitlist));

        lectureDetailRepository.save(lecture);
    }

    @Transactional
    @EventHandler
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(StudentDisenrolledEvent event) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<StudentDTO> enrolled = objectMapper.readerForListOf(StudentDTO.class).readValue(lecture.getEnrolledStudentsDtoJson());
        enrolled.removeIf(s -> s.id().equals(event.studentId()));
        lecture.setEnrolledStudentsDtoJson(objectMapper.writeValueAsString(enrolled));

        lectureDetailRepository.save(lecture);
    }

    @QueryHandler
    public LectureDetailDTO findById(FindLectureByIdQuery query) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(query.lectureId()).orElse(null);

        if (lecture == null) {
            return null;
        }

        return new LectureDetailDTO(
                lecture.getId(),
                objectMapper.readValue(lecture.getCourseDtoJson(), CourseDTO.class),
                lecture.getMaximumStudents(),
                objectMapper.readerForListOf(TimeSlot.class).readValue(lecture.getDatesJson()),
                objectMapper.readValue(lecture.getProfessorDtoJson(), ProfessorDTO.class),
                new HashSet<>(objectMapper.readerForListOf(StudentDTO.class).readValue(lecture.getEnrolledStudentsDtoJson())),
                objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson()),
                lecture.getLectureStatus(),
                new HashSet<>(objectMapper.readerForListOf(LectureAssessmentDTO.class).readValue(lecture.getAssessmentsJson()))
        );
    }

    @QueryHandler
    public EnrollmentStatusUpdate findEnrollmentStatus(EnrollmentStatusQuery query) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(query.lectureId()).orElseThrow();

        List<StudentDTO> enrolledStudents = objectMapper.readerForListOf(StudentDTO.class).readValue(lecture.getEnrolledStudentsDtoJson());
        List<WaitlistedStudentDTO> waitlistedStudents = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());

        EnrollmentStatus status = null;
        if (enrolledStudents.stream().anyMatch(student -> student.id().equals(query.studentId()))) {
            status = EnrollmentStatus.ENROLLED;
        } else if (waitlistedStudents.stream().anyMatch(w -> w.student().id().equals(query.studentId()))) {
            status = EnrollmentStatus.WAITLISTED;
        }

        return new EnrollmentStatusUpdate(
                lecture.getId(),
                query.studentId(),
                status
        );

    }

    @QueryHandler
    public WaitlistDTO getWaitlistForLecture(GetLectureWaitlistQuery query) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(query.lectureId()).orElseThrow(
                () -> new LectureNotFoundException(query.lectureId())
        );
        List<WaitlistedStudentDTO> waitlist = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());

        return new WaitlistDTO(
                waitlist.stream().map(w -> new WaitlistedStudentDTO(w.student(), w.waitlistedAt())).toList(),
                toSimpleDto(lecture)
        );
    }

    private SimpleLectureDTO toSimpleDto(LectureDetailProjectionEntity entity) throws JsonProcessingException {
        return new SimpleLectureDTO(
                entity.getId(),
                entity.getCourseId(),
                objectMapper.readValue(entity.getCourseDtoJson(), CourseDTO.class).name()
        );
    }

}
