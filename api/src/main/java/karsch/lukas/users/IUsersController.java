package karsch.lukas.users;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import karsch.lukas.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@RequestMapping("users")
@Tag(name = "Users", description = "Endpoints for managing users")
public interface IUsersController {

    @PostMapping("student")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> createStudent(@RequestBody @Valid CreateStudentRequest createStudentRequest);

    @PostMapping("professor")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<ApiResponse<UUID>> createProfessor(@RequestBody @Valid CreateProfessorRequest createProfessorRequest);
}
