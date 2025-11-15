package karsch.lukas.courses;

import karsch.lukas.context.RequestContext;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.CreateCourseRequest;
import karsch.lukas.course.ICoursesController;
import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CoursesController implements ICoursesController {

    private final RequestContext requestContext;

    private final static Logger log = LoggerFactory.getLogger(CoursesController.class);

    @Override
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCourses() {
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK, null, Collections.emptyList())
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> createCourse(CreateCourseRequest createCourseRequest) {
        log.info("this works. requestContext: {}", requestContext);
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, "Created"), HttpStatus.CREATED
        );
    }
}
