package karsch.lukas.stats;

import karsch.lukas.featureflags.Feature;
import karsch.lukas.featureflags.FeatureFlagService;
import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class StatsController implements IStatsController {

    private final StatsService statsService;
    private final FeatureFlagService featureFlagService;

    @Override
    public ResponseEntity<ApiResponse<AccumulatedCreditsResponse>> getAccumulatedCredits(Long studentId) {
        final AccumulatedCreditsResponse accumulatedCreditsResponse;

        if (featureFlagService.isEnabled(Feature.CUSTOM_QUERY_CREDITS_CALCULATION)) {
            accumulatedCreditsResponse = statsService.getAccumulatedCreditsCustomQuery(studentId);
        } else if (featureFlagService.isEnabled(Feature.IMPROVED_CREDITS_CALCULATION)) {
            accumulatedCreditsResponse = statsService.getAccumulatedCreditsImproved(studentId);
        } else {
            accumulatedCreditsResponse = statsService.getAccumulatedCredits(studentId);
        }

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, accumulatedCreditsResponse), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<GradesResponse>> getGrades(Long studentId) {
        var grades = statsService.getGradesForStudent(studentId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, grades), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<GradeHistoryResponse>> getGradesHistory(Long studentId, Long lectureId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }
}
