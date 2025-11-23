package karsch.lukas.lectures;

import karsch.lukas.context.RequestContext;
import karsch.lukas.lecture.*;
import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LecturesController implements ILecturesController {

    private final LecturesService lecturesService;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(Long studentId) {
        var lecturesForStudent = lecturesService.getLecturesForStudent(studentId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesForStudent), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(Long lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only users can enroll to courses"), HttpStatus.FORBIDDEN
            );
        }

        var enrollmentResult = lecturesService.enrollStudent(requestContext.getUserId(), lectureId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, new EnrollStudentResponse(enrollmentResult)),
                HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disenrollFromLecture(Long lectureId) {
        if (!"student".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only users can disenroll from courses"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.disenrollStudent(requestContext.getUserId(), lectureId);
        lecturesService.enrollNextEligibleStudentFromWaitlist(lectureId);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> createLectureFromCourse(CreateLectureRequest createLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can create lectures"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.createLectureFromCourse(requestContext.getUserId(), createLectureRequest);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(Long lectureId) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesService.getLectureDetails(lectureId)), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> assignGrade(Long lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can assign grades"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.assignGrade(lectureId, assignGradeRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> updateGrade(Long lectureId, AssignGradeRequest assignGradeRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can update grades"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.updateGrade(lectureId, assignGradeRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, null), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addDatesToLecture(Long lectureId, AssignDatesToLectureRequest assignDatesToLectureRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can add dates to a lecture"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.addDatesToLecture(assignDatesToLectureRequest, lectureId, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> addAssessmentForLecture(Long lectureId, CreateLectureAssessmentRequest createLectureAssessmentRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can add dates to a lecture"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.addAssessmentForLecture(lectureId, createLectureAssessmentRequest, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, null), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(Long lectureId) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, lecturesService.getWaitlistForLecture(lectureId)), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(Long lectureId, LectureStatus newLectureStatus) {
        if (!"professor".equals(requestContext.getUserType())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.FORBIDDEN, "Only professors can advance a lecture's lifecycle"), HttpStatus.FORBIDDEN
            );
        }

        lecturesService.advanceLifecycleOfLecture(lectureId, newLectureStatus, requestContext.getUserId());

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, String.format("Lifecycle advanced to %s", newLectureStatus)), HttpStatus.CREATED
        );
    }
}
