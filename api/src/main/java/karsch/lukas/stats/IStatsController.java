package karsch.lukas.stats;

import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@RequestMapping("stats")
public interface IStatsController {

    @GetMapping("credits")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<AccumulatedCreditsResponse>> getAccumulatedCredits(@RequestParam Long studentId);

    @GetMapping("grades")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<GradesResponse>> getGrades(@RequestParam Long studentId);

    @GetMapping("grades/history")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<GradeHistoryResponse>> getGradesHistory(@RequestParam Long studentId,
                                                                       @RequestParam Long lectureId,
                                                                       @RequestParam(required = false) LocalDateTime startDate,
                                                                       @RequestParam(required = false) LocalDateTime endDate
    );
}
