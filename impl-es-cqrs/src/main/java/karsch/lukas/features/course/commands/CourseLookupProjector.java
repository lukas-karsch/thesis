package karsch.lukas.features.course.commands;

import karsch.lukas.features.course.api.CourseCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

// application.properties -> subscribed to this processing group -> this component is immediately consistent
// lookup tables belong only to the command side, they are NOT read models
// https://www.axoniq.io/blog/2020set-based-consistency-validation
@ProcessingGroup(CourseAggregate.PROCESSING_GROUP)
@Component
@Slf4j
class CourseLookupProjector {

    @EventHandler
    public void on(CourseCreatedEvent event, CourseLookupRepository courseRepository) {
        log.debug("Handling {}, creating entry in lookup table.", event);
        courseRepository.save(
                new CoursesLookupJpaEntity(event.courseId(), event.credits(), event.minimumCreditsRequired(), new ArrayList<>(event.prerequisiteCourseIds()))
        );
    }
}
