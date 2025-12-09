package karsch.lukas.features.lectures.web;

import karsch.lukas.context.RequestContext;
import karsch.lukas.core.exceptions.QueryException;
import karsch.lukas.features.lectures.api.*;
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
        throw new RuntimeException();
    }

    @Override
    public ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(UUID lectureId) {
        throw new RuntimeException();
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disenrollFromLecture(UUID lectureId) {
        throw new RuntimeException();
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
            if (queryResult == null) {
                return new ResponseEntity<>(
                        new ApiResponse<>(HttpStatus.NOT_FOUND, "Lecture " + lectureId + " not found"),
                        HttpStatus.NOT_FOUND
                );
            }
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
        throw new RuntimeException();
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        throw new RuntimeException();
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addDatesToLecture(UUID lectureId, AssignDatesToLectureRequest assignDatesToLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.addDatesToLecture", requestContext.getUserType());
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Must be authenticated as professor to add dates to a lecture"), HttpStatus.FORBIDDEN
            );
        }

        commandGateway.sendAndWait(new AssignTimeSlotsToLectureCommand(
                lectureId,
                assignDatesToLectureRequest.dates(),
                requestContext.getUserId()
        ));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> addAssessmentForLecture(UUID lectureId, CreateLectureAssessmentRequest createLectureAssessmentRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.addAssessmentForLecture", requestContext.getUserType());
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Must be authenticated as professor to add assessments"), HttpStatus.FORBIDDEN
            );
        }

        UUID assessmentId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new AddAssessmentCommand(
                lectureId,
                assessmentId,
                createLectureAssessmentRequest.timeSlot(),
                createLectureAssessmentRequest.assessmentType(),
                createLectureAssessmentRequest.weight(),
                requestContext.getUserId()
        ));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, assessmentId), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(UUID lectureId) {
        throw new RuntimeException();
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(UUID lectureId, LectureStatus newLectureStatus) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.advanceLifecycleOfLecture", requestContext.getUserType());
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Must be authenticated as professor to create lectures"), HttpStatus.FORBIDDEN
            );
        }

        commandGateway.sendAndWait(new AdvanceLectureLifecycleCommand(lectureId, newLectureStatus, requestContext.getUserId()));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, "Advanced lifecycle"), HttpStatus.CREATED
        );
    }
}
