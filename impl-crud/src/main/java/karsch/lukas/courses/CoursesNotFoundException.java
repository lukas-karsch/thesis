package karsch.lukas.courses;

import java.util.Collection;

public class CoursesNotFoundException extends RuntimeException {
    public CoursesNotFoundException(Collection<Long> coursesNotFound) {
        var message = String.format("Courses with the following IDs were not found:  %s", coursesNotFound);
        super(message);
    }
}
