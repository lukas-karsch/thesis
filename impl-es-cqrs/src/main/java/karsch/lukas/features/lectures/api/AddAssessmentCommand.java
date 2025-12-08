package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.stats.AssessmentType;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record AddAssessmentCommand(
        @TargetAggregateIdentifier UUID lectureId,
        UUID assessmentId,
        TimeSlot timeSlot,
        AssessmentType assessmentType,
        float weight,
        UUID professorId) {
}
