package karsch.lukas.course;

import karsch.lukas.course.commands.CreateCourseCommand;
import karsch.lukas.course.events.CourseCreatedEvent;
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
public class Course {

    @AggregateIdentifier
    private UUID courseId;
    private String name;
    private String description;
    private int credits;
    private Set<Long> prerequisiteCourseIds;
    private int minimumCreditsRequired;

    @CommandHandler
    public Course(CreateCourseCommand cmd) {
        apply(new CourseCreatedEvent(
                cmd.courseId(),
                cmd.name(),
                cmd.description(),
                cmd.credits(),
                cmd.prerequisiteCourseIds(),
                cmd.minimumCreditsRequired()
        ));
    }

    @EventSourcingHandler
    public void on(CourseCreatedEvent event) {
        this.courseId = event.courseId();
        this.name = event.name();
        this.description = event.description();
        this.credits = event.credits();
        this.prerequisiteCourseIds = event.prerequisiteCourseIds();
        this.minimumCreditsRequired = event.minimumCreditsRequired();
    }
}
