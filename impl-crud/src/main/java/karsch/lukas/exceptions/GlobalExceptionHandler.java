package karsch.lukas.exceptions;

import karsch.lukas.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(
                new ApiResponse<>(ex.getStatusCode(), ex.getMessage()), ex.getStatusCode()
        );
    }
}
