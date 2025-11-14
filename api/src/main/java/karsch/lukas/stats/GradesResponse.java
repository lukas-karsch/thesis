package karsch.lukas.stats;

import java.util.List;

public record GradesResponse(Long studentId, List<GradeDTO> grades) {
}
