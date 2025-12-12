package karsch.lukas.features.enrollment.command;

import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.enrollment.command.lookup.IEnrollmentValidator;
import karsch.lukas.features.enrollment.exception.StudentNotEnrolledException;
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

    @CommandHandler
    public void handle(AssignGradeCommand command) {
        UUID enrollmentId = enrollmentValidator.getEnrollmentId(command.lectureId(), command.studentId())
                .orElseThrow(() -> new StudentNotEnrolledException(command.lectureId(), command.studentId()));

        // TODO validate professor
        // TODO validate assessment exists

        repository.load(enrollmentId.toString())
                .execute(enrollment -> enrollment.handle(command));
    }

}
