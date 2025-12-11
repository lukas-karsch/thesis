package karsch.lukas.courses;

import karsch.lukas.auth.NotAuthenticatedException;
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

import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CoursesController implements ICoursesController {

    private final RequestContext requestContext;

    private final CoursesService coursesService;

    private final static Logger log = LoggerFactory.getLogger(CoursesController.class);

    @Override
    public ResponseEntity<ApiResponse<Set<CourseDTO>>> getCourses() {
        var allCourses = coursesService.getAllCourses();

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK, allCourses), HttpStatus.OK
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createCourse(CreateCourseRequest createCourseRequest) {
        if (!"professor".equals(requestContext.getUserType())) {
            log.error("Invalid user type {} for CoursesController.createCourse", requestContext.getUserType());
            throw new NotAuthenticatedException("Must be authenticated as professor to create courses");
        }

        var created = coursesService.createCourse(createCourseRequest);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, "Created", created.getId()), HttpStatus.CREATED
        );
    }
}
