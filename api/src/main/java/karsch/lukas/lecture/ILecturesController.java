package karsch.lukas.lecture;

import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("lectures")
public interface ILecturesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<GetLecturesForStudentResponse>> getLecturesForStudent(@RequestParam Long studentId);

    @PostMapping("enroll")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> enrollToLecture(@RequestParam Long studentId, @RequestParam Long lectureId);

    @DeleteMapping("enroll")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> disenrollFromLecture(@RequestParam Long studentId, @RequestParam Long lectureId);

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> createLectureFromCourse(@RequestParam Long professorId, @RequestParam Long courseId);

    @GetMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<LectureDetailDTO>> getLectureDetails(@PathVariable Long lectureId);

    @PostMapping("{lectureId}")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> assignGrade(@PathVariable Long lectureId, @RequestParam Long professorId, @RequestBody AssignGradeRequest assignGradeRequest);

    @PatchMapping("{lectureId}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Void>> updateGrade(@PathVariable Long lectureId, @RequestParam Long professorId, @RequestBody AssignGradeRequest assignGradeRequest);
}
