package karsch.lukas.enrollment;

import karsch.lukas.courses.CourseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/enrollment")
public interface IEnrollmentController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<CourseDto> enrollToCourse(String courseId);
}
