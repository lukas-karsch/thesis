package karsch.lukas.stats;

import java.util.List;
import java.util.UUID;

public record GradesResponse(UUID studentId, List<GradeDTO> grades) {
}
