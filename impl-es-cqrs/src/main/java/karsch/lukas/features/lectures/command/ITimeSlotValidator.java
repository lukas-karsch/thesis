package karsch.lukas.features.lectures.command;

import karsch.lukas.lecture.TimeSlot;

import java.util.Collection;

public interface ITimeSlotValidator {
    boolean hasOverlap(Collection<TimeSlot> timeSlots);
}
