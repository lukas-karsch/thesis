package karsch.lukas.lecture;

import java.util.Collection;

public record AssignDatesToLectureRequest(Collection<TimeSlot> dates) {
}
