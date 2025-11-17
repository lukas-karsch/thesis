package karsch.lukas.users;

import karsch.lukas.mappers.Mapper;
import karsch.lukas.professor.ProfessorDTO;
import org.springframework.stereotype.Component;

@Component
public class ProfessorDtoMapper implements Mapper<ProfessorEntity, ProfessorDTO> {

    @Override
    public ProfessorDTO map(ProfessorEntity professorEntity) {
        return new ProfessorDTO(
                professorEntity.getId(),
                professorEntity.getFirstName(),
                professorEntity.getLastName()
        );
    }
}
