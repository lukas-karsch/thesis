package karsch.lukas.features.lectures.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record CreateEnrollmentCommand(@TargetAggregateIdentifier UUID enrollmentId, UUID lectureId, UUID studentId,
                                      UUID courseId) {
}
