package karsch.lukas.course;

import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;

@RestController
public class CoursesController implements ICoursesController {

    @Override
    public ResponseEntity<ApiResponse<Set<CourseDTO>>> getCourses() {
        var response = new ApiResponse<>(
                HttpStatus.OK,
                Set.of(
                        new CourseDTO(1L, "Maths", "Basic math topics", 5, Collections.emptySet())
                )
        );
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> createCourse(CreateCourseRequest createCourseRequest) {
        return null;
    }
}
