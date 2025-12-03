package karsch.lukas.lecture;

import java.util.UUID;

public record SimpleLectureDTO(Long id, UUID courseId, String courseName) {
}
