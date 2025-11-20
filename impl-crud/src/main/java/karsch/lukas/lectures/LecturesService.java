package karsch.lukas.lectures;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import karsch.lukas.auth.NotAuthenticatedException;
import karsch.lukas.courses.CoursesNotFoundException;
import karsch.lukas.courses.CoursesRepository;
import karsch.lukas.lecture.*;
import karsch.lukas.time.TimeSlotService;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorRepository;
import karsch.lukas.users.StudentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class LecturesService {

    private final LecturesRepository lecturesRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureWaitlistEntryRepository lectureWaitlistEntryRepository;
    private final CoursesRepository courseEntityRepository;
    private final ProfessorRepository professorRepository;
    private final LectureAssessmentRepository lectureAssessmentRepository;

    private final LectureDtoMapper lectureDtoMapper;
    private final LectureDetailDtoMapper lectureDetailDtoMapper;
    private final WaitlistedStudentMapper waitlistedStudentMapper;
    private final SimpleLectureDtoMapper simpleLectureDtoMapper;

    private final EntityManager entityManager;
    private final TimeSlotService timeSlotService;
    private final AssessmentGradeRepository assessmentGradeRepository;

    public GetLecturesForStudentResponse getLecturesForStudent(Long studentId) {
        var enrolledLectures = enrollmentRepository.findAllByStudentId(studentId)
                .stream()
                .map(EnrollmentEntity::getLecture)
                .map(lectureDtoMapper::map)
                .toList();

        var waitlistedLectures = lectureWaitlistEntryRepository.findAllByStudentId(studentId)
                .stream()
                .map(LectureWaitlistEntryEntity::getLecture)
                .map(lectureDtoMapper::map)
                .toList();

        return new GetLecturesForStudentResponse(enrolledLectures, waitlistedLectures);
    }

    /**
     * 1. Get lecture (throw if not exist) <br>
     * 2. Check if lecture is open for enrollment (else throw) <br>
     * 3. Check if student is enrolled (throw if they are)<br>
     * 4. Check if lecture is full<br>
     * 4.1 Full      -> waitlist the student <br>
     * 4.2 Not Full  -> enroll the student
     */
    @Transactional
    public EnrollmentStatus enrollStudent(Long studentId, Long lectureId) {
        // TODO check if the student has completed all prerequisites !
        log.warn("enrollStudent is still missing prerequisite checking!");
        var lecture = lecturesRepository
                .findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (lecture.getLectureStatus() != LectureStatus.OPEN_FOR_ENROLLMENT) {
            throw new LectureNotOpenForEnrollmentException(lectureId, lecture.getLectureStatus());
        }

        // check if the student is already enrolled
        var existingEnrollment = enrollmentRepository.findByStudentIdAndLectureId(studentId, lectureId);
        if (existingEnrollment.isPresent()) {
            throw new AlreadyEnrolledException(lectureId);
        }

        final StudentEntity studentReference = entityManager.getReference(StudentEntity.class, studentId);

        int enrolledStudents = enrollmentRepository.countByLecture(lecture);
        if (enrolledStudents >= lecture.getMaximumStudents()) {
            log.info("Lecture {} is full, student {} will be waitlisted", lectureId, studentId);
            var waitlistEntry = new LectureWaitlistEntryEntity();
            waitlistEntry.setLecture(lecture);
            waitlistEntry.setStudent(studentReference);
            lectureWaitlistEntryRepository.save(waitlistEntry);

            return EnrollmentStatus.WAITLISTED;
        } else {
            // not enrolled yet
            var enrollment = new EnrollmentEntity();
            enrollment.setLecture(lecture);
            enrollment.setStudent(studentReference);
            enrollmentRepository.save(enrollment);

            return EnrollmentStatus.ENROLLED;
        }
    }

    /**
     * Find the lecture. If exists and not archived or finished, delete enrollment and waitlist entries
     */
    @Transactional
    public void disenrollStudent(Long studentId, Long lectureId) {
        var lecture = lecturesRepository.findById(lectureId);
        if (lecture.isEmpty()
                || lecture.get().getLectureStatus() == LectureStatus.ARCHIVED
                || lecture.get().getLectureStatus() == LectureStatus.FINISHED
        ) {
            return;
        }

        enrollmentRepository.deleteByStudentIdAndLectureId(studentId, lectureId);
        lectureWaitlistEntryRepository.deleteByStudentIdAndLectureId(studentId, lectureId);
    }

    @Transactional
    public void createLectureFromCourse(Long professorId, CreateLectureRequest createLectureRequest) {
        var course = courseEntityRepository.findById(createLectureRequest.courseId())
                .orElseThrow(() -> new CoursesNotFoundException(
                        Collections.singleton(createLectureRequest.courseId())
                ));

        var professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new NotAuthenticatedException("Not authenticated as professor to create a lecture"));

        var lecture = new LectureEntity();
        lecture.setCourse(course);
        lecture.setProfessor(professor);
        lecture.setMaximumStudents(createLectureRequest.maximumStudents());
        lecture.getTimeSlots().addAll(createLectureRequest.dates().stream()
                .map(t -> new TimeSlotValueObject(t.date(), t.startTime(), t.endTime())
                ).collect(Collectors.toSet()));

        lecturesRepository.save(lecture);
    }

    public LectureDetailDTO getLectureDetails(Long lectureId) {
        var lecture = lecturesRepository.findDetailsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        return lectureDetailDtoMapper.map(lecture);
    }

    public WaitlistDTO getWaitlistForLecture(Long lectureId) {
        var lecture = lecturesRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        var waitlistEntries = lectureWaitlistEntryRepository.findByLectureOrderByCreatedDateAsc(lecture);

        return new WaitlistDTO(
                waitlistedStudentMapper.mapToList(waitlistEntries),
                simpleLectureDtoMapper.map(lecture)
        );
    }

    public void advanceLifecycleOfLecture(Long lectureId, LectureStatus newLectureStatus, Long professorId) {
        var lecture = lecturesRepository.findById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (newLectureStatus.ordinal() < lecture.getLectureStatus().ordinal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not revert a lecture's lifecycle to a previous state");
        }
        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException("Lectures can only be updated by the professor who created them");
        }

        lecture.setLectureStatus(newLectureStatus);

        lecturesRepository.save(lecture);
    }

    @Transactional
    public void addDatesToLecture(AssignDatesToLectureRequest assignDatesToLectureRequest, Long lectureId, Long professorId) {
        var lecture = lecturesRepository.findWithProfessorAndTimeSlotsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException();
        }

        var newTimeSlots = assignDatesToLectureRequest.dates().stream()
                .map(t -> new TimeSlotValueObject(t.date(), t.startTime(), t.endTime()))
                .collect(Collectors.toSet());

        lecture
                .getTimeSlots()
                .addAll(newTimeSlots);

        lecturesRepository.save(lecture);
    }

    @Transactional
    public void addAssessmentForLecture(Long lectureId, CreateLectureAssessmentRequest lectureAssessmentDTO, Long professorId) {
        // TODO: can assessments always be created? do i check that the weight is not > 1?
        var lecture = lecturesRepository.findDetailsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException();
        }

        final TimeSlot timeSlot = lectureAssessmentDTO.timeSlot();
        var assessment = new LectureAssessmentEntity();
        assessment.setLecture(lecture);
        assessment.setWeight(lectureAssessmentDTO.weight());
        assessment.setTimeSlot(new TimeSlotValueObject(
                timeSlot.date(),
                timeSlot.startTime(),
                timeSlot.endTime())
        );
        assessment.setAssessmentType(lectureAssessmentDTO.assessmentType());

        lectureAssessmentRepository.save(assessment);
    }

    @Transactional
    public void assignGrade(Long lectureId, AssignGradeRequest assignGradeRequest, Long professorId) {
        var lecture = lecturesRepository.findWithProfessorAndTimeSlotsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException();
        }

        var assessment = lectureAssessmentRepository.findById(assignGradeRequest.assessmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Assessment with ID %d not found.", assignGradeRequest.assessmentId())
                ));

        if (!timeSlotService.hasEnded(assessment.getTimeSlot())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can not assign grades for a assessment that has not ended");
        }

        var grade = new AssessmentGradeEntity();
        grade.setStudent(
                entityManager.getReference(StudentEntity.class, assignGradeRequest.studentId())
        );
        grade.setLectureAssessment(assessment);
        grade.setGrade(assignGradeRequest.grade());

        assessmentGradeRepository.save(grade);
    }

    @Transactional
    public void updateGrade(Long lectureId, AssignGradeRequest assignGradeRequest, Long professorId) {
        var lecture = lecturesRepository.findWithProfessorAndTimeSlotsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException();
        }

        var existingGrade = assessmentGradeRepository.findByStudentAndLectureAssessment(
                entityManager.getReference(StudentEntity.class, assignGradeRequest.studentId()),
                entityManager.getReference(LectureAssessmentEntity.class, assignGradeRequest.assessmentId())
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                String.format("No existing grade for student %d on assessment %d", assignGradeRequest.studentId(), assignGradeRequest.assessmentId())
        ));

        existingGrade.setGrade(assignGradeRequest.grade());
    }
}
