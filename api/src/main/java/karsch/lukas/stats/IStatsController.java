package karsch.lukas.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@RequestMapping("stats")
@Tag(name = "Statistics", description = "Endpoints for retrieving student statistics")
public interface IStatsController {

    @GetMapping("credits")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get accumulated credits", description = "Retrieves the total number of credits a student has accumulated from passed lectures.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved accumulated credits")
    })
    ResponseEntity<ApiResponse<AccumulatedCreditsResponse>> getAccumulatedCredits(@RequestParam UUID studentId);

    @GetMapping("grades")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get student grades", description = "Retrieves a summary of a student's grades for all their lectures.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved grades")
    })
    ResponseEntity<ApiResponse<GradesResponse>> getGrades(@RequestParam UUID studentId);

    @GetMapping("grades/history")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get grade history for an assessment", description = "Retrieves the history of grade changes for a specific student and lecture assessment, optionally filtered by a date range.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved grade history")
    })
    ResponseEntity<ApiResponse<GradeHistoryResponse>> getGradesHistory(@RequestParam UUID studentId,
                                                                       @RequestParam UUID lectureAssessmentId,
                                                                       @RequestParam(required = false) LocalDateTime startDate,
                                                                       @RequestParam(required = false) LocalDateTime endDate
    );
}
