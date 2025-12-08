package karsch.lukas.features.professor.queries;

import karsch.lukas.features.professor.api.FindProfessorByIdQuery;
import karsch.lukas.features.professor.api.ProfessorCreatedEvent;
import karsch.lukas.professor.ProfessorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ProcessingGroup("professors")
class ProfessorProjector {
    // TODO maybe move the read model for professor to lectures?

    private final ProfessorRepository repository;

    @EventHandler
    public void on(ProfessorCreatedEvent event) {
        var entity = new ProfessorProjectionEntity();

        entity.setId(event.id());
        entity.setFirstName(event.firstName());
        entity.setLastName(event.lastName());

        repository.save(entity);
    }

    @QueryHandler
    public ProfessorDTO findById(FindProfessorByIdQuery query) {
        return toDto(repository.findById(query.professorId()).orElse(null));
    }

    private ProfessorDTO toDto(ProfessorProjectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProfessorDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName()
        );
    }
}
