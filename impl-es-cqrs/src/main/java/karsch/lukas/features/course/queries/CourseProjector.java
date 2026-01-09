package karsch.lukas.features.course.queries;

import karsch.lukas.core.queries.CourseMapper;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.CourseCreatedEvent;
import karsch.lukas.features.course.api.FindAllCoursesQuery;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ProcessingGroup("courses")
@Slf4j
class CourseProjector {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @EventHandler
    public void on(CourseCreatedEvent event) {
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
        var all = courseRepository.findAll();

        return courseRepository.findAll().stream()
                .map(c -> courseMapper.map(
                        c,
                        () -> all.stream().filter(potentialP -> c.getPrerequisiteCourseIds().contains(potentialP.getId())).toList()
                ))
                .collect(Collectors.toSet());
    }

    @QueryHandler
    public Set<CourseDTO> handle(FindAllCoursesQuery query) {
        return findAll();
    }

    @QueryHandler
    public CourseDTO findById(FindCourseByIdQuery query) {
        return courseRepository.findById(query.courseId())
                .map(c -> courseMapper.map(c, () -> courseRepository.findAllById(c.getPrerequisiteCourseIds())))
                .orElse(null);
    }
}
