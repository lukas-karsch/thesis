package karsch.lukas.features.course.queries;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.course.api.FindCoursesByIdsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ProcessingGroup("courses")
@XSlf4j
class CourseProjector {

    private final CourseRepository courseRepository;

    @EventHandler
    public void on(CourseCreatedEvent event) {
        log.entry(event);

        CourseProjectionEntity courseEntity = new CourseProjectionEntity();
        courseEntity.setId(event.courseId());
        courseEntity.setName(event.name());
        courseEntity.setDescription(event.description());
        courseEntity.setCredits(event.credits());
        courseEntity.setPrerequisiteCourseIds(event.prerequisiteCourseIds());
        courseEntity.setMinimumCreditsRequired(event.minimumCreditsRequired());

        courseRepository.save(courseEntity);
        log.exit();
    }

    private Set<CourseDTO> findAll() {
        return courseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindAllCoursesQuery query) {
        log.entry(query);
        return log.exit(findAll());
    }

    private Set<CourseDTO> findByIds(Set<UUID> ids) {
        return log.exit(courseRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toSet()));
    }

    @QueryHandler
    public CourseDTO findById(FindCourseByIdQuery query) {
        log.entry(query);
        return log.exit(courseRepository.findById(query.courseId()).map(this::toDto).orElse(null));
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindCoursesByIdsQuery query) {
        log.entry(query);
        return log.exit(findByIds(query.courseIds()));
    }

    private CourseDTO toDto(CourseProjectionEntity entity) {
        var prerequisites = courseRepository
                .findAllById(entity.getPrerequisiteCourseIds())
                .stream()
                .map(p -> new SimpleCourseDTO(p.getId(), p.getName(), p.getDescription(), p.getCredits()))
                .collect(Collectors.toSet());

        return new CourseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCredits(),
                prerequisites,
                entity.getMinimumCreditsRequired()
        );
    }
}
