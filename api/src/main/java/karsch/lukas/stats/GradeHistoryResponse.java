package karsch.lukas.stats;

import java.util.List;
import java.util.UUID;

/**
 * Contains the grade history for a lecture.
 */
public record GradeHistoryResponse(UUID studentId, UUID lectureId, List<GradeChangeDTO> history) {
}
