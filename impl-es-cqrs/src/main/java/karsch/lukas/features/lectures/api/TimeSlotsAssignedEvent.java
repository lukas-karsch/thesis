package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;

import java.util.List;
import java.util.UUID;

public record TimeSlotsAssignedEvent(UUID lectureId, List<TimeSlot> newTimeSlots, UUID professorId) {
}
