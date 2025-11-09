package karsch.lukas.enrollment;

import karsch.lukas.courses.CourseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentController implements IEnrollmentController {

    @Override
    public ResponseEntity<CourseDto> enrollToCourse(String courseId) {
        return new ResponseEntity<>(new CourseDto("1", "Mathematics"), HttpStatus.CREATED);
    }
}
