package karsch.lukas.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ApiResponse<T> {
    @JsonIgnore
    private final HttpStatusCode httpStatus;

    private final String status;

    private final int code;

    private final String message;

    private final T data;

    private final LocalDateTime timestamp;

    public ApiResponse(HttpStatusCode httpStatus, String message, T data) {
        this.httpStatus = httpStatus;
        this.status = httpStatus.is2xxSuccessful() ? "success" : "error";
        this.code = httpStatus.value();
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(HttpStatusCode httpStatus, String message) {
        this(httpStatus, message, null);
    }
}
