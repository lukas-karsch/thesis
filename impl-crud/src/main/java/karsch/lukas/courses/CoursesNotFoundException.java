package karsch.lukas.courses;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

public class CoursesNotFoundException extends ResponseStatusException {
    public CoursesNotFoundException(Collection<Long> coursesNotFound) {
        var message = String.format("Courses with the following IDs were not found:  %s", coursesNotFound);
        super(HttpStatus.NOT_FOUND, message);
    }
}
