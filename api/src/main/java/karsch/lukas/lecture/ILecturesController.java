package karsch.lukas.lecture;

import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("lectures")
public interface ILecturesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(@RequestParam UUID studentId);

    @PostMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<EnrollStudentResponse>> enrollToLecture(@PathVariable UUID lectureId);

    @DeleteMapping("{lectureId}/enroll")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> disenrollFromLecture(@PathVariable UUID lectureId);

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> createLectureFromCourse(@RequestBody @Valid CreateLectureRequest createLectureRequest);

    @GetMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(@PathVariable UUID lectureId);

    @PostMapping("{lectureId}")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> assignGrade(@PathVariable UUID lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PatchMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> updateGrade(@PathVariable UUID lectureId, @RequestBody @Valid AssignGradeRequest assignGradeRequest);

    @PostMapping("{lectureId}/dates")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> addDatesToLecture(@PathVariable UUID lectureId, @RequestBody @Valid AssignDatesToLectureRequest assignDatesToLectureRequest);

    @PostMapping("{lectureId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> addAssessmentForLecture(@PathVariable UUID lectureId, @RequestBody @Valid CreateLectureAssessmentRequest createLectureAssessmentRequest);

    @GetMapping("{lectureId}/waitingList")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<WaitlistDTO>> getWaitingListForLecture(@PathVariable UUID lectureId);

    @PostMapping("{lectureId}/lifecycle")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> advanceLifecycleOfLecture(@PathVariable UUID lectureId, @RequestParam LectureStatus newLectureStatus);
}
