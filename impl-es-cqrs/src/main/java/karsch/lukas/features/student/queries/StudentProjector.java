package karsch.lukas.features.student.queries;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import karsch.lukas.features.student.api.FindStudentByIdQuery;
import karsch.lukas.features.student.api.StudentCreatedEvent;
import karsch.lukas.student.StudentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ProcessingGroup("students")
@RequiredArgsConstructor
public class StudentProjector {

    private final StudentRepository studentRepository;

    @EventHandler
    @Transactional
    public void on(StudentCreatedEvent event) {
        var entity = new StudentProjectionEntity(
                event.studentId(),
                event.firstName(),
                event.lastName(),
                event.semester()
        );
        log.debug("projected student: {}", entity);
        studentRepository.save(entity);
    }

    @QueryHandler
    public @Nullable StudentDTO findById(FindStudentByIdQuery query) {
        return studentRepository.findById(query.studentId())
                .map(s -> new StudentDTO(s.getId(), s.getFirstName(), s.getLastName()))
                .orElse(null);
    }

}
