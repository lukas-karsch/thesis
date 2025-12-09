package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Collection;
import java.util.UUID;

public record AssignTimeSlotsToLectureCommand(
        @TargetAggregateIdentifier UUID lectureId,
        Collection<TimeSlot> dates,
        UUID professorId
) {
}
