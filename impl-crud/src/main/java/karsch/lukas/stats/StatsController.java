package karsch.lukas.stats;

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

    @Override
    public ResponseEntity<ApiResponse<AccumulatedCreditsResponse>> getAccumulatedCredits(Long studentId) {
        AccumulatedCreditsResponse accumulatedCreditsResponse = statsService.getAccumulatedCredits(studentId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, accumulatedCreditsResponse), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<GradesResponse>> getGrades(Long studentId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<GradeHistoryResponse>> getGradesHistory(Long studentId, Long lectureId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }
}
