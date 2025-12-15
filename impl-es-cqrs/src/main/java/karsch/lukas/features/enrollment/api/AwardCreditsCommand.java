package karsch.lukas.features.enrollment.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.UUID;

public record AwardCreditsCommand(@TargetAggregateIdentifier UUID enrollmentId, List<UUID> allAssessmentIds) {
}
