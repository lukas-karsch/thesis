package karsch.lukas.features.lectures.web;

import karsch.lukas.context.RequestContext;
import karsch.lukas.core.exceptions.QueryException;
import karsch.lukas.features.lectures.api.CreateLectureCommand;
import karsch.lukas.features.lectures.api.FindLectureByIdQuery;
import karsch.lukas.lecture.*;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.uuid.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LecturesController implements ILecturesController {

    private final RequestContext requestContext;

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    @Override
    public ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(UUID studentId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(UUID lectureId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disenrollFromLecture(UUID lectureId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createLectureFromCourse(CreateLectureRequest createLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.createLectureFromCourse", requestContext.getUserType());
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Must be authenticated as professor to create lectures"), HttpStatus.FORBIDDEN
            );
        }

        var lectureId = UuidUtils.randomV7();
        var command = new CreateLectureCommand(
                lectureId,
                createLectureRequest.courseId(),
                createLectureRequest.maximumStudents(),
                createLectureRequest.dates(),
                requestContext.getUserId()
        );

        UUID result = commandGateway.sendAndWait(command);

        Assert.isTrue(lectureId.equals(result), "lectureId and result are not equal. lectureId=" + lectureId + ", result=" + result);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, lectureId), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(UUID lectureId) {
        try {
            var queryResult = queryGateway.query(new FindLectureByIdQuery(lectureId), ResponseTypes.instanceOf(LectureDetailDTO.class)).get();
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.OK, queryResult),
                    HttpStatus.OK
            );
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching lecture {}", lectureId, e);
            throw new QueryException("Error fetching lecture " + lectureId);
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> assignGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addDatesToLecture(UUID lectureId, AssignDatesToLectureRequest assignDatesToLectureRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> addAssessmentForLecture(UUID lectureId, CreateLectureAssessmentRequest createLectureAssessmentRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(UUID lectureId) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(UUID lectureId, LectureStatus newLectureStatus) {
        return null;
    }
}
