package karsch.lukas.stats;

import java.time.LocalDateTime;
import java.util.List;

public record GradesResponse(Long studentId, List<GradeDTO> grades, LocalDateTime assignedAt) {
}
