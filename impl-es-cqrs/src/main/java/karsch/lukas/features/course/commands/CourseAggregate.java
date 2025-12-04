package karsch.lukas.features.course.commands;

import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.CreateCourseCommand;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.Set;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
public class CourseAggregate {

    @AggregateIdentifier
    private UUID courseId;
    private String name;
    private String description;
    private int credits;
    private Set<UUID> prerequisiteCourseIds;
    private int minimumCreditsRequired;

    @CommandHandler
    public CourseAggregate(CreateCourseCommand cmd) {
        apply(new CourseCreatedEvent(
                cmd.getCourseId(),
                cmd.getName(),
                cmd.getDescription(),
                cmd.getCredits(),
                cmd.getPrerequisiteCourseIds(),
                cmd.getMinimumCreditsRequired()
        ));
    }

    @EventSourcingHandler
    public void on(CourseCreatedEvent event) {
        this.courseId = event.getCourseId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.credits = event.getCredits();
        this.prerequisiteCourseIds = event.getPrerequisiteCourseIds();
        this.minimumCreditsRequired = event.getMinimumCreditsRequired();
    }
}
