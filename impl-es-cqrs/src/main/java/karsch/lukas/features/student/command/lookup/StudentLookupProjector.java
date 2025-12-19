package karsch.lukas.features.student.command.lookup;

import karsch.lukas.features.student.api.StudentCreatedEvent;
import karsch.lukas.features.student.command.StudentAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ProcessingGroup(StudentAggregate.PROCESSING_GROUP)
@RequiredArgsConstructor
class StudentLookupProjector {

    private final StudentLookupRepository studentLookupRepository;

    @EventHandler
    public void on(StudentCreatedEvent event) {
        var entity = new StudentLookupEntity(event.studentId(), event.semester());
        studentLookupRepository.save(entity);
    }
}
