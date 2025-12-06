package karsch.lukas.infra.web;

import karsch.lukas.core.exceptions.ErrorDetails;
import karsch.lukas.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(
                new ApiResponse<>(ex.getStatusCode(), ex.getMessage()),
                ex.getStatusCode()
        );
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleAxonException(CommandExecutionException ex) {
        Optional<Object> details = ex.getDetails();

        if (details.isPresent() && ErrorDetails.RESOURCE_NOT_FOUND.equals(details.get())) {
            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage()),
                    HttpStatus.NOT_FOUND
            );
        }

        log.error(ex.getMessage(), ex);

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
