package karsch.lukas.stats;

import java.util.UUID;

public record AccumulatedCreditsResponse(UUID studentId, int totalCredits) {
}
