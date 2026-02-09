package karsch.lukas.features.stats.queries.credits;

import jakarta.transaction.Transactional;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.stats.api.GetCreditsForStudentQuery;
import karsch.lukas.stats.AccumulatedCreditsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ProcessingGroup("credits")
class StudentCreditsProjector {

    private final StudentCreditsProjectionRepository studentCreditsRepository;

    private final QueryGateway queryGateway;
    private final CourseProjectionRepository courseProjectionRepository;

    @EventHandler
    @Transactional
    public void on(CourseCreatedEvent event) {
        var entity = new CourseProjectionEntity(event.courseId(), event.credits());
        courseProjectionRepository.save(entity);
    }

    @EventHandler
    @Transactional
    @Retryable(retryFor = {IllegalStateException.class})
    public void on(CreditsAwardedEvent event) {
        log.debug("Projecting {}", event);
        if (!event.hasPassed()) {
            return;
        }
        var course = courseProjectionRepository.findById(event.courseId())
                .orElseThrow(() -> new IllegalStateException("Course " + event.courseId() + " not found via FindCourseByIdQuery"));

        var creditsEntity = studentCreditsRepository.findById(event.studentId())
                .orElseGet(() -> new StudentCreditsProjectionEntity(event.studentId(), 0));

        int currentCredits = creditsEntity.getTotalCredits();

        creditsEntity.setTotalCredits(currentCredits + course.getCredits());

        studentCreditsRepository.save(creditsEntity);
    }

    @QueryHandler
    public AccumulatedCreditsResponse getCreditsForStudent(GetCreditsForStudentQuery query) {
        return studentCreditsRepository
                .findById(query.studentId())
                .map(r -> new AccumulatedCreditsResponse(r.getId(), r.getTotalCredits()))
                .orElse(new AccumulatedCreditsResponse(query.studentId(), 0));
    }
}
