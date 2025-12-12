package karsch.lukas.features.lectures.web;

import karsch.lukas.context.RequestContext;
import karsch.lukas.core.auth.NotAuthenticatedException;
import karsch.lukas.core.exceptions.ErrorDetails;
import karsch.lukas.core.exceptions.NotAllowedException;
import karsch.lukas.core.exceptions.QueryException;
import karsch.lukas.features.enrollment.api.AssignGradeCommand;
import karsch.lukas.features.enrollment.api.UpdateGradeCommand;
import karsch.lukas.features.lectures.api.*;
import karsch.lukas.features.lectures.queries.EnrollmentStatusUpdate;
import karsch.lukas.lecture.*;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.uuid.UuidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
        try {
            var queryResult = queryGateway.query(new GetLecturesForStudentQuery(studentId), ResponseTypes.instanceOf(GetLecturesForStudentResponse.class)).get();
            if (queryResult == null) {
                throw new QueryExecutionException("Lectures for " + studentId + " not found", null, ErrorDetails.RESOURCE_NOT_FOUND);
            }
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.OK, queryResult),
                    HttpStatus.OK
            );
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching lectures for student {}", studentId, e);
            throw new QueryException("Error fetching lectures for student " + studentId);
        }
    }

    @Override
    public ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(UUID lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.enrollToLecture", requestContext.getUserType());
            throw new NotAllowedException("Must be authenticated as student to enroll");
        }

        try (var subscription = queryGateway.subscriptionQuery(
                new EnrollmentStatusQuery(lectureId, requestContext.getUserId()),
                ResponseTypes.instanceOf(EnrollmentStatusUpdate.class),
                ResponseTypes.instanceOf(EnrollmentStatusUpdate.class))
        ) {
            Mono<EnrollmentStatusUpdate> enrollmentResult = subscription.initialResult()
                    .flatMap(initial -> {
                        log.info("initial result: {}", initial);
                        // If the initial state says they are already enrolled, return it immediately
                        if (initial != null && EnrollmentStatus.ENROLLED.equals(initial.status())) {
                            return Mono.just(initial);
                        }
                        // Otherwise, switch to waiting for updates
                        return subscription.updates().next().handle((u, s) -> {
                            log.info("Update: {}", u);
                            s.next(u);
                        });
                    });

            commandGateway.sendAndWait(new EnrollStudentCommand(lectureId, requestContext.getUserId()));

            EnrollmentStatusUpdate result = enrollmentResult.block(Duration.ofSeconds(10));

            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.CREATED, new EnrollStudentResponse(result.status())), HttpStatus.CREATED
            );
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disenrollFromLecture(UUID lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.enrollToLecture", requestContext.getUserType());
            throw new NotAllowedException("Must be authenticated as student to enroll");
        }

        commandGateway.sendAndWait(new DisenrollStudentCommand(lectureId, requestContext.getUserId()));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createLectureFromCourse(CreateLectureRequest createLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.createLectureFromCourse", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to create lectures");
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
                throw new QueryExecutionException("Lecture " + lectureId + " not found", null, ErrorDetails.RESOURCE_NOT_FOUND);
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
    public ResponseEntity<ApiResponse<Void>> assignGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.assignGrade", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to assign grades");
        }

        commandGateway.sendAndWait(new AssignGradeCommand(
                assignGradeRequest.assessmentId(),
                lectureId,
                assignGradeRequest.studentId(),
                assignGradeRequest.grade(),
                requestContext.getUserId())
        );

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.updateGrade", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to update grades");
        }

        commandGateway.sendAndWait(new UpdateGradeCommand(
                assignGradeRequest.assessmentId(),
                lectureId,
                assignGradeRequest.studentId(),
                assignGradeRequest.grade(),
                requestContext.getUserId())
        );

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addDatesToLecture(UUID lectureId, AssignDatesToLectureRequest assignDatesToLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.addDatesToLecture", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to add dates to a lecture");
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
            throw new NotAuthenticatedException("Must be authenticated as professor to add assessments");
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
        var queryResult = queryGateway.query(new GetLectureWaitlistQuery(lectureId), ResponseTypes.instanceOf(WaitlistDTO.class)).join();
        if (queryResult == null) {
            throw new QueryExecutionException("Could not load waitlist for lecture" + lectureId, null, ErrorDetails.RESOURCE_NOT_FOUND);
        }
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, queryResult),
                HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(UUID lectureId, LectureStatus newLectureStatus) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for LecturesController.advanceLifecycleOfLecture", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to create lectures");
        }

        commandGateway.sendAndWait(new AdvanceLectureLifecycleCommand(lectureId, newLectureStatus, requestContext.getUserId()));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, "Advanced lifecycle", null), HttpStatus.CREATED
        );
    }
}
