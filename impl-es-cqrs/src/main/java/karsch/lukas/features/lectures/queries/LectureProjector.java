package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import karsch.lukas.core.queries.CourseMapper;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.lectures.exceptions.LectureNotFoundException;
import karsch.lukas.features.professor.api.ProfessorCreatedEvent;
import karsch.lukas.features.student.api.StudentCreatedEvent;
import karsch.lukas.lecture.*;
import karsch.lukas.professor.ProfessorDTO;
import karsch.lukas.student.StudentDTO;
import karsch.lukas.time.TimeSlotComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static karsch.lukas.core.json.Defaults.EMPTY_LIST;

@Component
@Slf4j
@ProcessingGroup("lectures")
@RequiredArgsConstructor
class LectureProjector {

    private final LectureDetailRepository lectureDetailRepository;
    private final ObjectMapper objectMapper;
    private final QueryUpdateEmitter updateEmitter;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private final CourseMapper courseMapper;

    @EventHandler
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(LectureCreatedEvent event) throws JsonProcessingException {
        var professor = professorRepository.findById(event.professorId())
                .orElseThrow(() -> new NoSuchElementException("Professor " + event.professorId() + " not found"));

        var course = courseRepository.findById(event.courseId())
                .map(c -> courseMapper.map(c, () -> courseRepository.findAllById(c.getPrerequisiteCourseIds())))
                .orElseThrow(() -> new NoSuchElementException("Course " + event.courseId() + " not found"));

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
    @Retryable(retryFor = {NoSuchElementException.class})
    public void on(StudentEnrolledEvent event) throws JsonProcessingException {
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<StudentDTO> enrolledStudents = objectMapper.readerForListOf(StudentDTO.class).readValue(lecture.getEnrolledStudentsDtoJson());

        if (enrolledStudents.stream().anyMatch(s -> Objects.equals(s.id(), event.studentId()))) {
            return;
        }

        // FIXME this is probably still a bug: no guarantee that events on other aggregates are processed before this projector runs
        // if student data IS necessary for a projection, it should be included in the event
        var student = studentRepository.findById(event.studentId())
                .map(this::toStudentDto)
                .orElseThrow(() -> new NoSuchElementException("Student " + event.studentId() + " not found"));

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
        var lecture = lectureDetailRepository.findById(event.lectureId()).orElseThrow();

        List<WaitlistedStudentDTO> waitlist = objectMapper.readerForListOf(WaitlistedStudentDTO.class).readValue(lecture.getWaitingListDtoJson());

        var student = studentRepository.findById(event.studentId())
                .map(this::toStudentDto)
                .orElseThrow(() -> new NoSuchElementException("Student " + event.studentId() + " not found"));

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

    @Transactional
    @EventHandler
    public void on(ProfessorCreatedEvent event) {
        var entity = new ProfessorProjectionEntity();

        entity.setId(event.id());
        entity.setFirstName(event.firstName());
        entity.setLastName(event.lastName());

        log.debug("projected professor: {}", entity);
        professorRepository.save(entity);
    }

    @EventHandler
    @Transactional
    public void on(StudentCreatedEvent event) {
        var entity = new StudentProjectionEntity(
                event.studentId(),
                event.firstName(),
                event.lastName(),
                event.semester()
        );
        log.debug("projected student: {}", entity);
        studentRepository.save(entity);
    }

    @EventHandler
    public void on(CourseCreatedEvent event) {
        CourseProjectionEntity courseEntity = new CourseProjectionEntity();
        courseEntity.setId(event.courseId());
        courseEntity.setName(event.name());
        courseEntity.setDescription(event.description());
        courseEntity.setCredits(event.credits());
        courseEntity.setPrerequisiteCourseIds(event.prerequisiteCourseIds());
        courseEntity.setMinimumCreditsRequired(event.minimumCreditsRequired());

        courseRepository.save(courseEntity);
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
                toSimplLectureDto(lecture)
        );
    }

    private SimpleLectureDTO toSimplLectureDto(LectureDetailProjectionEntity entity) throws JsonProcessingException {
        return new SimpleLectureDTO(
                entity.getId(),
                entity.getCourseId(),
                objectMapper.readValue(entity.getCourseDtoJson(), CourseDTO.class).name()
        );
    }

    private StudentDTO toStudentDto(StudentProjectionEntity student) {
        return new StudentDTO(student.getId(), student.getFirstName(), student.getLastName());
    }

}
