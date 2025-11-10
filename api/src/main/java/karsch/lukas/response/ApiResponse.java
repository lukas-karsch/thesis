package karsch.lukas.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ApiResponse<T> {
    private final ResponseType result;
    private final T data;

    public static <T> ApiResponse<T> error(T data) {
        return new ApiResponse<T>(ResponseType.ERROR, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ResponseType.SUCCESS, data);
    }
}
