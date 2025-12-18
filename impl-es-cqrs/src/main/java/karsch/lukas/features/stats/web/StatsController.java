package karsch.lukas.features.stats.web;

import karsch.lukas.core.exceptions.ErrorDetails;
import karsch.lukas.features.stats.api.GetCreditsForStudentQuery;
import karsch.lukas.features.stats.api.GetGradesForStudentQuery;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.stats.AccumulatedCreditsResponse;
import karsch.lukas.stats.GradeHistoryResponse;
import karsch.lukas.stats.GradesResponse;
import karsch.lukas.stats.IStatsController;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StatsController implements IStatsController {

    private final QueryGateway queryGateway;

    @Override
    public ResponseEntity<ApiResponse<AccumulatedCreditsResponse>> getAccumulatedCredits(UUID studentId) {
        var creditsResponse = queryGateway.query(new GetCreditsForStudentQuery(studentId), ResponseTypes.instanceOf(AccumulatedCreditsResponse.class)).join();
        if (creditsResponse == null) {
            throw new QueryExecutionException("Student " + studentId + " not found", null, ErrorDetails.RESOURCE_NOT_FOUND);
        }

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, creditsResponse), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<GradesResponse>> getGrades(UUID studentId) {
        var gradesResponse = queryGateway.query(new GetGradesForStudentQuery(studentId), ResponseTypes.instanceOf(GradesResponse.class)).join();
        if (gradesResponse == null) {
            throw new QueryExecutionException("Student " + studentId + " not found", null, ErrorDetails.RESOURCE_NOT_FOUND);
        }

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, gradesResponse), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<GradeHistoryResponse>> getGradesHistory(UUID studentId, UUID lectureAssessmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }
}
