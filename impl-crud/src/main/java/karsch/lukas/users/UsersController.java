package karsch.lukas.users;

import karsch.lukas.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UsersController implements IUsersController {
    private final UsersService usersService;

    @Override
    public ResponseEntity<ApiResponse<UUID>> createStudent(CreateStudentRequest createStudentRequest) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, usersService.createStudent(createStudentRequest)), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createProfessor(CreateProfessorRequest createProfessorRequest) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, usersService.createProfessor(createProfessorRequest)), HttpStatus.CREATED
        );
    }
}
