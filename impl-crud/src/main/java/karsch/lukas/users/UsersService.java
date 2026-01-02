package karsch.lukas.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;

    public UUID createStudent(CreateStudentRequest createStudentRequest) {
        var student = new StudentEntity();
        student.setFirstName(createStudentRequest.firstName());
        student.setLastName(createStudentRequest.lastName());
        studentRepository.save(student);

        return student.getId();
    }

    public UUID createProfessor(CreateProfessorRequest createProfessorRequest) {
        var professor = new ProfessorEntity();
        professor.setFirstName(createProfessorRequest.firstName());
        professor.setLastName(createProfessorRequest.lastName());
        professorRepository.save(professor);

        return professor.getId();
    }
}
