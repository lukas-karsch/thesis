package karsch.lukas.features.lectures.queries;

import karsch.lukas.features.professor.api.ProfessorCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ProcessingGroup("professors")
class ProfessorProjector {

    private final ProfessorRepository repository;

    @EventHandler
    public void on(ProfessorCreatedEvent event) {
        var entity = new ProfessorProjectionEntity();

        entity.setId(event.id());
        entity.setFirstName(event.firstName());
        entity.setLastName(event.lastName());

        repository.save(entity);
    }
}
