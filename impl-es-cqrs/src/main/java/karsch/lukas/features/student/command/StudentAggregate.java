package karsch.lukas.features.student.command;

import karsch.lukas.features.student.api.CreateStudentCommand;
import karsch.lukas.features.student.api.StudentCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class StudentAggregate {
    @AggregateIdentifier
    private UUID id;

    @CommandHandler
    public StudentAggregate(CreateStudentCommand command) {
        apply(new StudentCreatedEvent(
                command.id(),
                command.firstName(),
                command.lastName()
        ));
    }

    protected StudentAggregate() {
    }

    @EventSourcingHandler
    public void on(StudentCreatedEvent event) {
        this.id = event.studentId();
    }

}
