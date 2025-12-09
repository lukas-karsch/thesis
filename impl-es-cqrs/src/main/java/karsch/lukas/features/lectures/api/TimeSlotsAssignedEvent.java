package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.TimeSlot;

import java.util.TreeSet;
import java.util.UUID;

public record TimeSlotsAssignedEvent(UUID lectureId, TreeSet<TimeSlot> newTimeSlots, UUID professorId) {
}
