package karsch.lukas.lectures;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AlreadyEnrolledException extends ResponseStatusException {
    public AlreadyEnrolledException(Long lectureId) {
        super(HttpStatus.BAD_REQUEST, String.format("Already enrolled to lecture with id %d", lectureId));
    }
}
