package karsch.lukas.stats;

import java.time.LocalDateTime;
import java.util.UUID;

public record GradeChangeDTO(UUID assessmentId, int grade, LocalDateTime changedAt) {
}
