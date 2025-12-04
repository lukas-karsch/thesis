package karsch.lukas.lecture;

import java.util.UUID;

public record SimpleLectureDTO(UUID id, UUID courseId, String courseName) {
}
