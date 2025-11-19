package karsch.lukas.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotAuthenticatedException extends ResponseStatusException {
    public NotAuthenticatedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public NotAuthenticatedException() {
        this("Not authenticated");
    }
}
