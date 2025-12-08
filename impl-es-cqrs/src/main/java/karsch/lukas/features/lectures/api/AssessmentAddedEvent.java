package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.stats.AssessmentType;

import java.util.UUID;

public record AssessmentAddedEvent(
        UUID lectureId,
        UUID assessmentId,
        TimeSlot timeSlot,
        AssessmentType assessmentType,
        float weight) {
}
