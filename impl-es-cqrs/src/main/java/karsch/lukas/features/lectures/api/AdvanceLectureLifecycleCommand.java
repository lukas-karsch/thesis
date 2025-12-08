package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.LectureStatus;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record AdvanceLectureLifecycleCommand(@TargetAggregateIdentifier UUID id, LectureStatus lectureStatus,
                                             UUID professorId) {
}
