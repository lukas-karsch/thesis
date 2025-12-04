package karsch.lukas.lectures;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

class AlreadyEnrolledException extends ResponseStatusException {
    public AlreadyEnrolledException(UUID lectureId) {
        super(HttpStatus.BAD_REQUEST, String.format("Already enrolled to lecture with id %s", lectureId));
    }
}
