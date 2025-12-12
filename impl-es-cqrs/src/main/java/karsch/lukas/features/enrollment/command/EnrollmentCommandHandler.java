package karsch.lukas.features.enrollment.command;

import karsch.lukas.core.exceptions.DomainException;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.enrollment.command.lookup.IEnrollmentValidator;
import karsch.lukas.features.enrollment.exception.AssessmentNotFoundException;
import karsch.lukas.features.enrollment.exception.StudentNotEnrolledException;
import karsch.lukas.features.lectures.command.lookup.lecture.ILectureValidator;
import karsch.lukas.features.lectures.exceptions.LectureNotFoundException;
import karsch.lukas.lecture.LectureStatus;
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

    @CommandHandler
    public void handle(AssignGradeCommand command) {
        UUID enrollmentId = enrollmentValidator.getEnrollmentId(command.lectureId(), command.studentId())
                .orElseThrow(() -> new StudentNotEnrolledException(command.lectureId(), command.studentId()));

        var lecture = lectureValidator.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException(command.lectureId()));

        if (!lecture.getProfessorId().equals(command.professorId())) {
            throw new NotAllowedException("Professor " + command.professorId() + " is not allowed to assign grades to lecture " + command.lectureId());
        }

        if (!lecture.getAssessmentIds().contains(command.assessmentId())) {
            throw new AssessmentNotFoundException(command.assessmentId());
        }

        if (lecture.getLectureStatus() != LectureStatus.FINISHED) {
            throw new DomainException("Can not assign grades to a lecture that isn't finished");
        }

        repository.load(enrollmentId.toString())
                .execute(enrollment -> enrollment.handle(command));
    }

}
