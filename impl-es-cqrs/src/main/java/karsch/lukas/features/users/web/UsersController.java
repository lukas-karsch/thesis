package karsch.lukas.features.users.web;

import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.features.student.api.CreateStudentCommand;
import karsch.lukas.response.ApiResponse;
import karsch.lukas.users.CreateProfessorRequest;
import karsch.lukas.users.CreateStudentRequest;
import karsch.lukas.users.IUsersController;
import karsch.lukas.uuid.UuidUtils;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UsersController implements IUsersController {

    private final CommandGateway commandGateway;

    @Override
    public ResponseEntity<ApiResponse<UUID>> createStudent(CreateStudentRequest createStudentRequest) {
        UUID studentId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateStudentCommand(studentId, createStudentRequest.firstName(), createStudentRequest.lastName(), 1));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, studentId), HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UUID>> createProfessor(CreateProfessorRequest createProfessorRequest) {
        UUID professorId = UuidUtils.randomV7();
        commandGateway.sendAndWait(new CreateProfessorCommand(professorId, createProfessorRequest.firstName(), createProfessorRequest.lastName()));

        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.CREATED, professorId), HttpStatus.CREATED
        );
    }
}
