package karsch.lukas.course;

import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("courses")
public interface ICoursesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<List<CourseDTO>>> getCourses();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<Void>> createCourse(@RequestParam Long professorId, @RequestBody CreateCourseRequest createCourseRequest);
}
