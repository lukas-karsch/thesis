package karsch.lukas.features.lectures.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record ConfirmStudentEnrollmentCommand(@TargetAggregateIdentifier UUID lectureId, UUID studentId) {
}
