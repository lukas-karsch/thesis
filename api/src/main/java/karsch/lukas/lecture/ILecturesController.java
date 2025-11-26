package karsch.lukas.lecture;

import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("lectures")
public interface ILecturesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(@RequestParam Long studentId);

    @PostMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(@PathVariable Long lectureId);

    @DeleteMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> disenrollFromLecture(@PathVariable Long lectureId);

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> createLectureFromCourse(@RequestBody @Valid CreateLectureRequest createLectureRequest);

    @GetMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(@PathVariable Long lectureId);

    @PostMapping("{lectureId}")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> assignGrade(@PathVariable Long lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PatchMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> updateGrade(@PathVariable Long lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PostMapping("{lectureId}/dates")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> addDatesToLecture(@PathVariable Long lectureId, @RequestBody @Valid AssignDatesToLectureRequest assignDatesToLectureRequest);

    @PostMapping("{lectureId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> addAssessmentForLecture(@PathVariable Long lectureId, @RequestBody @Valid CreateLectureAssessmentRequest createLectureAssessmentRequest);

    @GetMapping("{lectureId}/waitingList")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(@PathVariable Long lectureId);

    @PostMapping("{lectureId}/lifecycle")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(@PathVariable Long lectureId, @RequestParam LectureStatus newLectureStatus);
}
