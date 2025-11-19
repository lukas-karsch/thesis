package karsch.lukas.lecture;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateLectureRequest(Long courseId, @Positive int maximumStudents, @NotEmpty List<TimeSlot> dates) {
}
