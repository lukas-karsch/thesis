package karsch.lukas.features.course.queries;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.features.course.api.FindCoursesByIdsQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ProcessingGroup("courses")
public class CourseProjection {

    private final CourseRepository courseRepository;

    @EventHandler
    public void on(CourseCreatedEvent event) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(event.getCourseId());
        courseEntity.setName(event.getName());
        courseEntity.setDescription(event.getDescription());
        courseEntity.setCredits(event.getCredits());
        courseEntity.setPrerequisiteCourseIds(event.getPrerequisiteCourseIds());
        courseEntity.setMinimumCreditsRequired(event.getMinimumCreditsRequired());

        courseRepository.save(courseEntity);
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindAllCoursesQuery query) {
        return courseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindCoursesByIdsQuery query) {
        return courseRepository.findAllById(query.getCourseIds()).stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    private CourseDTO toDto(CourseEntity entity) {
        Set<SimpleCourseDTO> prerequisites = entity.getPrerequisiteCourseIds() == null ? Collections.emptySet() : entity.getPrerequisiteCourseIds().stream()
                .map(id -> new SimpleCourseDTO(id, null, null, -1)) // TODO
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
