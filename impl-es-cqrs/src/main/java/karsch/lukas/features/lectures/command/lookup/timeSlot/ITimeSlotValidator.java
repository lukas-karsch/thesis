package karsch.lukas.features.lectures.command.lookup.timeSlot;

import karsch.lukas.lecture.TimeSlot;

import java.util.SortedSet;
import java.util.UUID;

public interface ITimeSlotValidator {

    boolean overlapsWithOtherLectures(SortedSet<TimeSlot> timeSlots, UUID studentId);
}
