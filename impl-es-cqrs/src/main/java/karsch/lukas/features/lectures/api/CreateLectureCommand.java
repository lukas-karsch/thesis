package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.UUID;

public record CreateLectureCommand(
        @TargetAggregateIdentifier UUID id,
        UUID courseId,
        int maximumStudents,
        List<TimeSlot> dates,
        UUID professorId
) {
}
