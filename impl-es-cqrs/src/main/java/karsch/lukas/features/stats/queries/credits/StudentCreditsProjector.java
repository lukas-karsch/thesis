package karsch.lukas.features.stats.queries.credits;

import jakarta.transaction.Transactional;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.stats.api.GetCreditsForStudentQuery;
import karsch.lukas.stats.AccumulatedCreditsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
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

    @EventHandler
    @Transactional
    @Retryable(retryFor = {IllegalStateException.class})
    public void on(CreditsAwardedEvent event) {
        if (!event.hasPassed()) {
            return;
        }
        var courseFuture = queryGateway.query(new FindCourseByIdQuery(event.courseId()), ResponseTypes.instanceOf(CourseDTO.class));

        var creditsEntity = studentCreditsRepository.findById(event.studentId())
                .orElseGet(() -> new StudentCreditsProjectionEntity(event.studentId(), 0));

        int currentCredits = creditsEntity.getTotalCredits();

        var course = courseFuture.join();
        if (course == null) {
            throw new IllegalStateException("Course " + event.courseId() + " not found via FindCourseByIdQuery");
        }
        creditsEntity.setTotalCredits(currentCredits + course.credits());

        studentCreditsRepository.save(creditsEntity);
    }

    @QueryHandler
    public AccumulatedCreditsResponse getCreditsForStudent(GetCreditsForStudentQuery query) {
        return studentCreditsRepository
                .findById(query.studentId())
                .map(r -> new AccumulatedCreditsResponse(r.getId(), r.getTotalCredits()))
                .orElse(null);
    }
}
