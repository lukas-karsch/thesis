package karsch.lukas.lectures;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

class LectureNotFoundException extends ResponseStatusException {
    public LectureNotFoundException(UUID lectureId) {
        super(HttpStatus.NOT_FOUND, String.format("Lecture with id %s not found", lectureId));
    }
}
