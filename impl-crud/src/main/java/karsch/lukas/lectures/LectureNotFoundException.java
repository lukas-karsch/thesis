package karsch.lukas.lectures;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LectureNotFoundException extends ResponseStatusException {
    public LectureNotFoundException(Long lectureId) {
        super(HttpStatus.NOT_FOUND, String.format("Lecture with id %d not found", lectureId));
    }
}
