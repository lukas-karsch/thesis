package karsch.lukas.features.enrollment.command.lookup.credits;

import karsch.lukas.features.course.commands.ICourseValidator;
import karsch.lukas.features.enrollment.api.CreditsAwardedEvent;
import karsch.lukas.features.enrollment.command.EnrollmentAggregate;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup(EnrollmentAggregate.PROCESSING_GROUP)
@RequiredArgsConstructor
class StudentCreditsLookupProjector {

    private final ICourseValidator courseValidator;
    private final StudentCreditsLookupRepository studentCreditsLookupRepository;

    @EventHandler
    public void on(CreditsAwardedEvent event) {
        if (!event.hasPassed()) {
            return;
        }

        var creditsEntity = studentCreditsLookupRepository.findById(event.studentId())
                .orElseGet(() -> new StudentCreditsLookupProjectionEntity(event.studentId(), 0));

        int currentCredits = creditsEntity.getCredits();
        int courseIsWorth = courseValidator.getCreditsForCourse(event.courseId());
        creditsEntity.setCredits(currentCredits + courseIsWorth);

        studentCreditsLookupRepository.save(creditsEntity);
    }

}
