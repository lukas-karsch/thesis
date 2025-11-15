package karsch.lukas.lecture;

import java.util.Set;

public record AssignDatesToLectureRequest(Set<TimeSlot> dates) {
}
