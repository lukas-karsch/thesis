package karsch.lukas.infra.web;

import karsch.lukas.core.exceptions.ErrorDetails;
import karsch.lukas.core.exceptions.QueryException;
import karsch.lukas.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

        if (details.isEmpty() || !(details.get() instanceof ErrorDetails errorDetails)) {
            log.error(ex.getMessage(), ex);

            return new ResponseEntity<>(
                    new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        final HttpStatus status = switch (errorDetails) {
            case ErrorDetails.RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorDetails.ILLEGAL_DOMAIN_STATE -> HttpStatus.BAD_REQUEST;
            case ErrorDetails.NOT_ALLOWED -> HttpStatus.FORBIDDEN;
        };

        return new ResponseEntity<>(
                new ApiResponse<>(status, ex.getMessage()),
                status
        );
    }

    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(AggregateNotFoundException ex) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(QueryException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(QueryException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()),
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }
}
