package karsch.lukas.stats;

import java.util.List;

/**
 * Contains the grade history for a lecture.
 */
public record GradeHistoryResponse(Long studentId, Long lectureId, List<GradeChangeDTO> history) {
}
