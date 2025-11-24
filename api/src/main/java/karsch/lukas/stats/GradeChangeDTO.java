package karsch.lukas.stats;

import java.time.LocalDateTime;

public record GradeChangeDTO(long assessmentId, int grade, LocalDateTime changedAt) {
}
