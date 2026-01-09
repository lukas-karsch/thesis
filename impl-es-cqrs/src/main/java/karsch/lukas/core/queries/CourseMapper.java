package karsch.lukas.core.queries;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CourseMapper {

    public CourseDTO map(ICourseProjectionEntity entity, Supplier<List<? extends ICourseProjectionEntity>> getPrerequisites) {
        var prerequisites = getPrerequisites.get().stream()
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
