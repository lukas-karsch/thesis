package karsch.lukas.features.professor.command;

import karsch.lukas.features.professor.api.ProfessorCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup(ProfessorAggregate.PROCESSING_GROUP) // subscribed in application.properties
@Slf4j
class ProfessorLookupTable {

    @EventHandler
    public void on(ProfessorCreatedEvent event, ProfessorLookupRepository professorLookupRepository) {
        log.debug("Handling {}, creating entry in lookup table.", event);
        professorLookupRepository.save(
                new ProfessorLookupEntity(event.id())
        );
    }
}
