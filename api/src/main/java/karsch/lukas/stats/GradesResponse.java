package karsch.lukas.stats;

import java.util.List;
import java.util.UUID;

/**
 * DTO which contains all grades for a student
 */
public record GradesResponse(UUID studentId, List<GradeDTO> grades) {
}
