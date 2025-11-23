package karsch.lukas.users;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StudentNotFoundException extends ResponseStatusException {
    public StudentNotFoundException(long studentId) {
        super(HttpStatus.NOT_FOUND, String.format("Student with ID %s not found", studentId));
    }
}
