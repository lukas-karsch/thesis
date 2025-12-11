package karsch.lukas.lectures;

import karsch.lukas.auth.NotAuthenticatedException;
import karsch.lukas.context.RequestContext;
import karsch.lukas.lecture.*;
import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LecturesController implements ILecturesController {

    private final LecturesService lecturesService;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(UUID studentId) {
        var lecturesForStudent = lecturesService.getLecturesForStudent(studentId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesForStudent), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(UUID lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only users can enroll to courses");
        }

        var enrollmentResult = lecturesService.enrollStudent(requestContext.getUserId(), lectureId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, new EnrollStudentResponse(enrollmentResult)),
                HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disenrollFromLecture(UUID lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only users can disenroll from courses");
        }

        lecturesService.disenrollStudent(requestContext.getUserId(), lectureId);
        lecturesService.enrollNextEligibleStudentFromWaitlist(lectureId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createLectureFromCourse(CreateLectureRequest createLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can create lectures");
        }

        var created = lecturesService.createLectureFromCourse(requestContext.getUserId(), createLectureRequest);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, created.getId()), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(UUID lectureId) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesService.getLectureDetails(lectureId)), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> assignGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can assign grades");
        }

        var grade = lecturesService.assignGrade(lectureId, assignGradeRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, grade.getId()), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateGrade(UUID lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can update grades");
        }

        lecturesService.updateGrade(lectureId, assignGradeRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addDatesToLecture(UUID lectureId, AssignDatesToLectureRequest assignDatesToLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can add dates to a lecture");
        }

        lecturesService.addDatesToLecture(assignDatesToLectureRequest, lectureId, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> addAssessmentForLecture(UUID lectureId, CreateLectureAssessmentRequest createLectureAssessmentRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can add dates to a lecture");
        }

        var created = lecturesService.addAssessmentForLecture(lectureId, createLectureAssessmentRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, created.getId()), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(UUID lectureId) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesService.getWaitlistForLecture(lectureId)), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(UUID lectureId, LectureStatus newLectureStatus) {
        if (!"professor".equals(requestContext.getUserType())) {
            throw new NotAuthenticatedException("Only professors can advance a lecture's lifecycle");
        }

        lecturesService.advanceLifecycleOfLecture(lectureId, newLectureStatus, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, String.format("Lifecycle advanced to %s", newLectureStatus), null), HttpStatus.CREATED
        );
    }
}
