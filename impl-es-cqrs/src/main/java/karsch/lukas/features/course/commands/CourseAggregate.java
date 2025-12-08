package karsch.lukas.features.course.commands;

import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.CreateCourseCommand;
import karsch.lukas.features.course.exceptions.MissingCoursesException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.Set;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@Aggregate
class CourseAggregate {

    static final String PROCESSING_GROUP = "course_commands";

    @AggregateIdentifier
    private UUID courseId;
    private String name;
    private String description;
    private int credits;
    private Set<UUID> prerequisiteCourseIds;
    private int minimumCreditsRequired;

    @CommandHandler
    public CourseAggregate(CreateCourseCommand cmd, ICourseValidator courseValidator) {
        log.debug("Handling {}", cmd);
        if (!courseValidator.allCoursesExist(cmd.prerequisiteCourseIds())) {
            throw new MissingCoursesException("Some prerequisites don't exist.");
        }

        log.debug("Applying CourseCreatedEvent");
        apply(new CourseCreatedEvent(
                cmd.courseId(),
                cmd.name(),
                cmd.description(),
                cmd.credits(),
                cmd.prerequisiteCourseIds(),
                cmd.minimumCreditsRequired()
        ));
    }

    protected CourseAggregate() {
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
