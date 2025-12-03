package karsch.lukas.lecture;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateLectureRequest(UUID courseId, @Positive int maximumStudents, @NotEmpty List<TimeSlot> dates) {
}
