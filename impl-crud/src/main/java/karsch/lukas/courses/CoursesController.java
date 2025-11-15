package karsch.lukas.courses;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.CreateCourseRequest;
import karsch.lukas.course.ICoursesController;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class CoursesController implements ICoursesController {

    @Override
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCourses() {
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK, null, Collections.emptyList())
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> createCourse(Long professorId, CreateCourseRequest createCourseRequest) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, "Created"), HttpStatus.CREATED
        );
    }
}
