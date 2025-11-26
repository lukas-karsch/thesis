package karsch.lukas.lectures;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import karsch.lukas.auth.NotAuthenticatedException;
import karsch.lukas.courses.CoursesNotFoundException;
import karsch.lukas.courses.CoursesRepository;
import karsch.lukas.lecture.*;
import karsch.lukas.stats.StatsService;
import karsch.lukas.time.TimeSlotService;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorRepository;
import karsch.lukas.users.StudentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final AssessmentGradeRepository assessmentGradeRepository;

    private final LectureDtoMapper lectureDtoMapper;
    private final LectureDetailDtoMapper lectureDetailDtoMapper;
    private final WaitlistedStudentMapper waitlistedStudentMapper;
    private final SimpleLectureDtoMapper simpleLectureDtoMapper;

    private final EntityManager entityManager;

    private final TimeSlotService timeSlotService;
    private final StatsService statsService;

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
     * 4. Check if student is enrolled to a lecture with overlapping timeslots (and throw) <br>
     * 5. Check if student completed all prerequisites
     * 6. Check if lecture is full<br>
     * 6.1 Full      -> waitlist the student <br>
     * 6.2 Not Full  -> enroll the student
     */
    @Transactional
    public EnrollmentStatus enrollStudent(Long studentId, Long lectureId) {
        log.debug("Student {} wants to enroll to lecture {}", studentId, lectureId);

        var lecture = lecturesRepository
                .findWithCourseAndPrerequisitesById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (lecture.getLectureStatus() != LectureStatus.OPEN_FOR_ENROLLMENT) {
            log.debug("Lecture {} is not open for enrollment.", lectureId);
            throw new LectureNotOpenForEnrollmentException(lectureId, lecture.getLectureStatus());
        }

        // check if the student is already enrolled
        var existingEnrollment = enrollmentRepository.findByStudentIdAndLectureId(studentId, lectureId);
        if (existingEnrollment.isPresent()) {
            log.debug("Student {} is already enrolled to lecture {}", studentId, lectureId);
            throw new AlreadyEnrolledException(lectureId);
        }

        enrollmentRepository.findAllWithTimeSlotsByStudentId(studentId)
                .stream()
                .filter(e -> timeSlotService.areConflictingTimeSlots(
                        e.getLecture().getTimeSlots(), lecture.getTimeSlots()
                ))
                .findFirst()
                .ifPresent(_ -> {
                    log.debug("Can not enroll student {} to lecture {} because of conflicting timeslots with another lecture.", studentId, lectureId);
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "This lecture has conflicting timeslots with another lecture.");
                });

        final StudentEntity studentReference = entityManager.getReference(StudentEntity.class, studentId);

        var course = lecture.getCourse();
        var passedLectures = statsService.getPassedLectures(studentReference).toList();
        long completedPrerequisitesCount = passedLectures.stream()
                .map(LectureEntity::getCourse)
                .filter(course.getPrerequisites()::contains)
                .count();

        if (completedPrerequisitesCount != course.getPrerequisites().size()) {
            log.debug("Student {} has not completed all prerequisites for lecture {}", studentId, lectureId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student has not completed all prerequisites.");
        }

        int studentCredits = statsService.countCreditsFromLectures(passedLectures.stream());
        if (studentCredits < lecture.getMinimumCreditsRequired()) {
            log.debug("Student {} has not earned enough credits for lecture {} (has {}, needs {})", studentId, lectureId, studentCredits, lecture.getMinimumCreditsRequired());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student has not earned enough credits to enroll");
        }

        int enrolledStudents = enrollmentRepository.countByLecture(lecture);
        if (enrolledStudents >= lecture.getMaximumStudents()) {
            var allEnrollments = enrollmentRepository.findAllByLecture(lecture);
            log.info("all enrollments for lecture {}: {}", lectureId, allEnrollments);
            log.info("Lecture {} is full, student {} will be waitlisted", lectureId, studentId);
            var waitlistEntry = new LectureWaitlistEntryEntity();
            waitlistEntry.setLecture(lecture);
            waitlistEntry.setStudent(studentReference);
            lectureWaitlistEntryRepository.save(waitlistEntry);

            return EnrollmentStatus.WAITLISTED;
        } else {
            // not enrolled yet
            log.info("Enrolling student {} to lecture {}", studentId, lectureId);
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
        var lecture = lecturesRepository.findWithEnrollmentsAndWaitlistById(lectureId);
        if (lecture.isEmpty()
                || lecture.get().getLectureStatus() == LectureStatus.FINISHED
                || lecture.get().getLectureStatus() == LectureStatus.ARCHIVED
        ) {
            return;
        }

        log.info("Disenrolling student {} from lecture {}", studentId, lectureId);

        lecture.get().getEnrollments().removeIf(enrollment -> enrollment.getStudent().getId().equals(studentId));
        lecture.get().getWaitlist().removeIf(waitlistEntry -> waitlistEntry.getStudent().getId().equals(studentId));
    }

    /**
     * Enrolls the next eligible student from the waitlist to the specified lecture
     */
    @Transactional
    public void enrollNextEligibleStudentFromWaitlist(long lectureId) {
        var lecture = lecturesRepository.findWithEnrollmentsAndWaitlistById(lectureId).orElse(null);

        if (lecture == null) {
            return;
        }

        var sortedWaitlist = lecture
                .getWaitlist()
                .stream()
                .sorted(new LectureWaitlistEntryComparator())
                .collect(Collectors.toCollection(ArrayList::new));

        if (sortedWaitlist.isEmpty()) {
            log.info("Waitlist for lecture {} is empty, can not enroll another student.", lectureId);
            return;
        }

        var eligibleWaitlistEntry = sortedWaitlist.getLast();
        log.info("Try to enroll eligible student {} to lecture {} from waitlist", eligibleWaitlistEntry.getStudent().getId(), lectureId);

        enrollStudent(eligibleWaitlistEntry.getStudent().getId(), lectureId);

        lecture.getWaitlist().remove(eligibleWaitlistEntry);
        lecturesRepository.save(lecture);
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

        final List<TimeSlotValueObject> timeSlots = createLectureRequest.dates().stream()
                .map(t -> new TimeSlotValueObject(t.date(), t.startTime(), t.endTime()))
                .toList();

        if (timeSlotService.containsOverlappingTimeslots(timeSlots)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overlapping or duplicate time slots are not allowed");
        }

        lecture.getTimeSlots().addAll(timeSlots);

        log.info("Professor {} is creating a lecture from course {}", professorId, createLectureRequest.courseId());

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

    @Transactional
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

        if (newLectureStatus.ordinal() >= LectureStatus.IN_PROGRESS.ordinal()) {
            // delete waitlist entries when the lecture is set to IN_PROGRESS
            // no further enrollments are possible!
            lecture.getWaitlist().clear();
        }

        log.info("Lifecycle of lecture {} set to {}", lectureId, newLectureStatus);

        lecturesRepository.save(lecture);
    }

    @Transactional
    public void addDatesToLecture(AssignDatesToLectureRequest assignDatesToLectureRequest, Long lectureId, Long professorId) {
        var lecture = lecturesRepository.findWithProfessorAndTimeSlotsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException("Dates can only be assigned to a lecture by the professor who owns the lecture");
        }

        var newTimeSlots = assignDatesToLectureRequest.dates().stream()
                .map(t -> new TimeSlotValueObject(t.date(), t.startTime(), t.endTime()))
                .toList();

        if (timeSlotService.containsOverlappingTimeslots(newTimeSlots)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Overlapping or duplicate time slots are not allowed");
        }

        lecture
                .getTimeSlots()
                .addAll(newTimeSlots);

        log.info("Added {} new timeslots to lecture {}", newTimeSlots.size(), lectureId);

        lecturesRepository.save(lecture);
    }

    @Transactional
    public void addAssessmentForLecture(Long lectureId, CreateLectureAssessmentRequest lectureAssessmentDTO, Long professorId) {
        // TODO: think - can assessments always be created? do i check that the weight is not > 1?
        var lecture = lecturesRepository.findDetailsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException();
        }

        final TimeSlot timeSlot = lectureAssessmentDTO.timeSlot();

        var assessment = new LectureAssessmentEntity();
        assessment.setLecture(lecture);
        assessment.setWeight(lectureAssessmentDTO.weight());
        var timeSlotValueObject = new TimeSlotValueObject(
                timeSlot.date(),
                timeSlot.startTime(),
                timeSlot.endTime()
        );
        if (timeSlotService.hasEnded(timeSlotValueObject)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date for this assessment is in the past.");
        }
        assessment.setTimeSlot(timeSlotValueObject);
        assessment.setAssessmentType(lectureAssessmentDTO.assessmentType());

        lectureAssessmentRepository.save(assessment);
    }

    @Transactional
    public void assignGrade(Long lectureId, AssignGradeRequest assignGradeRequest, Long professorId) {
        var lecture = lecturesRepository.findWithProfessorAndTimeSlotsById(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        if (!Objects.equals(lecture.getProfessor().getId(), professorId)) {
            throw new NotAuthenticatedException("Grades can only be assigned by the professor who owns the lecture");
        }

        var assessment = lectureAssessmentRepository.findById(assignGradeRequest.assessmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Assessment with ID %d not found.", assignGradeRequest.assessmentId())
                ));

        var studentIsEnrolledToLecture = enrollmentRepository.existsByStudentIdAndLectureId(assignGradeRequest.studentId(), lectureId);
        if (!studentIsEnrolledToLecture) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Student %d is not enrolled to lecture %d", assignGradeRequest.studentId(), lectureId)
            );
        }

        if (!(lecture.getLectureStatus() == LectureStatus.IN_PROGRESS || lecture.getLectureStatus() == LectureStatus.FINISHED)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Lecture is in invalid state %s, can not assign a grade", lecture.getLectureStatus())
            );
        }

        if (!timeSlotService.hasEnded(assessment.getTimeSlot())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not assign grades for a assessment that has not ended");
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
