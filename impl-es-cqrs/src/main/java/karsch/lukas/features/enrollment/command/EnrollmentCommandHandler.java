package karsch.lukas.features.enrollment.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.enrollment.api.UpdateGradeCommand;
import karsch.lukas.features.enrollment.command.lookup.IEnrollmentValidator;
import karsch.lukas.features.enrollment.exception.AssessmentNotFoundException;
import karsch.lukas.features.enrollment.exception.StudentNotEnrolledException;
import karsch.lukas.features.lectures.command.lookup.assessment.AssessmentLookupEntity;
import karsch.lukas.features.lectures.command.lookup.assessment.IAssessmentValidator;
import karsch.lukas.features.lectures.command.lookup.lecture.ILectureValidator;
import karsch.lukas.features.lectures.command.lookup.lecture.LectureLookupEntity;
import karsch.lukas.features.lectures.exceptions.LectureNotFoundException;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.time.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentCommandHandler {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final Repository<EnrollmentAggregate> repository;
    private final IEnrollmentValidator enrollmentValidator;
    private final ILectureValidator lectureValidator;
    private final IAssessmentValidator assessmentValidator;
    private final TimeSlotService timeSlotService;

    // TODO remove duplicate code

    @CommandHandler
    public void handle(AssignGradeCommand command) {
        UUID enrollmentId = getEnrollmentId(command.lectureId(), command.studentId());

        LectureLookupEntity lecture = lectureValidator.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException(command.lectureId()));

        if (!lecture.getProfessorId().equals(command.professorId())) {
            throw new NotAllowedException("Professor " + command.professorId() + " is not allowed to assign grades to lecture " + command.lectureId());
        }

        if (lecture.getLectureStatus() != LectureStatus.IN_PROGRESS) {
            throw new DomainException("Can only assign grades to a lecture which is IN_PROGRESS");
        }

        AssessmentLookupEntity assessment = assessmentValidator.findById(command.assessmentId())
                .orElseThrow(() -> new AssessmentNotFoundException(command.assessmentId()));

        if (!timeSlotService.hasEnded(assessment.getTimeSlot(), t -> new TimeSlot(t.date(), t.startTime(), t.endTime()))) {
            throw new DomainException("Assessment has not yet ended (timeSlot=" + assessment.getTimeSlot() + "), system time=" + timeSlotService.getCurrentTime());
        }

        repository.load(enrollmentId.toString())
                .execute(enrollment -> enrollment.handle(command, timeSlotService));
    }

    @CommandHandler
    public void handle(UpdateGradeCommand command) {
        UUID enrollmentId = getEnrollmentId(command.lectureId(), command.studentId());

        LectureLookupEntity lecture = lectureValidator.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException(command.lectureId()));

        if (!lecture.getProfessorId().equals(command.professorId())) {
            throw new NotAllowedException("Professor " + command.professorId() + " is not allowed to assign grades to lecture " + command.lectureId());
        }

        repository.load(enrollmentId.toString())
                .execute(enrollment -> enrollment.handle(command, timeSlotService));
    }

    private UUID getEnrollmentId(UUID lectureId, UUID studentId) {
        return enrollmentValidator.getEnrollmentId(lectureId, studentId)
                .orElseThrow(() -> new StudentNotEnrolledException(lectureId, studentId));
    }

}
