package karsch.lukas.features.student.command;

import karsch.lukas.features.student.api.CreateStudentCommand;
import karsch.lukas.features.student.api.StudentCreatedEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTriggerDefinition")
public class StudentAggregate {

    public static final String PROCESSING_GROUP = "student_commands";

    @AggregateIdentifier
    private UUID id;
    private int semester;

    @CommandHandler
    public StudentAggregate(CreateStudentCommand command) {
        apply(new StudentCreatedEvent(
                command.id(),
                command.firstName(),
                command.lastName(),
                command.semester()
        ));
    }

    @EventSourcingHandler
    public void on(StudentCreatedEvent event) {
        this.id = event.studentId();
        this.semester = event.semester();
    }

}
