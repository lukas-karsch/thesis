package karsch.lukas.features.course.queries;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.course.api.FindCoursesByIdsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
class CourseProjector {

    private final CourseRepository courseRepository;

    @EventHandler
    public void on(CourseCreatedEvent event) {
        log.debug("handling event {}", event);

        CourseProjectionEntity courseEntity = new CourseProjectionEntity();
        courseEntity.setId(event.courseId());
        courseEntity.setName(event.name());
        courseEntity.setDescription(event.description());
        courseEntity.setCredits(event.credits());
        courseEntity.setPrerequisiteCourseIds(event.prerequisiteCourseIds());
        courseEntity.setMinimumCreditsRequired(event.minimumCreditsRequired());

        courseRepository.save(courseEntity);
    }

    private Set<CourseDTO> findAll() {
        return courseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindAllCoursesQuery query) {
        return findAll();
    }

    private Set<CourseDTO> findByIds(Set<UUID> ids) {
        return courseRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    @QueryHandler
    public CourseDTO findById(FindCourseByIdQuery query) {
        return courseRepository.findById(query.courseId()).map(this::toDto).orElse(null);
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindCoursesByIdsQuery query) {
        return findByIds(query.courseIds());
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
