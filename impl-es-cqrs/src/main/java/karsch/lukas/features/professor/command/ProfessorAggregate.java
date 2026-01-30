package karsch.lukas.features.professor.command;

import karsch.lukas.features.professor.api.CreateProfessorCommand;
import karsch.lukas.features.professor.api.ProfessorCreatedEvent;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "snapshotTriggerDefinition")
@NoArgsConstructor
@Slf4j
class ProfessorAggregate {

    static final String PROCESSING_GROUP = "professor_commands";

    @AggregateIdentifier
    private UUID id;

    @CommandHandler
    public ProfessorAggregate(CreateProfessorCommand command) {
        apply(new ProfessorCreatedEvent(command.id(), command.firstName(), command.lastName()));
    }

    @EventSourcingHandler
    public void on(ProfessorCreatedEvent event) {
        this.id = event.id();
        //  first and last name are only interesting for the read model -> no need to store them in the aggregate
    }

}
