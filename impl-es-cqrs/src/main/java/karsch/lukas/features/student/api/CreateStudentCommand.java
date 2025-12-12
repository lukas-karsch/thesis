package karsch.lukas.features.student.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record CreateStudentCommand(
        @TargetAggregateIdentifier UUID id,
        String firstName,
        String lastName,
        int semester) {
}
