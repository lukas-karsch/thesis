package karsch.lukas.users;

import karsch.lukas.mappers.Mapper;
import karsch.lukas.student.StudentDTO;
import org.springframework.stereotype.Component;

@Component
public class StudentDtoMapper implements Mapper<StudentEntity, StudentDTO> {
    @Override
    public StudentDTO map(StudentEntity studentEntity) {
        return new StudentDTO(
                studentEntity.getId(),
                studentEntity.getFirstName(),
                studentEntity.getLastName()
        );
    }
}
