package karsch.lukas.course;

import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RequestMapping("courses")
public interface ICoursesController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<ApiResponse<Set<CourseDTO>>> getCourses();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> createCourse(@RequestBody @Valid CreateCourseRequest createCourseRequest);
}
