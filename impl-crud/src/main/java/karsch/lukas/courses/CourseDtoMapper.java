package karsch.lukas.courses;

import karsch.lukas.course.CourseAssessmentDTO;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseDtoMapper implements Mapper<CourseEntity, CourseDTO> {

    private final SimpleCourseDtoMapper simpleCourseDtoMapper;

    @Override
    public CourseDTO map(CourseEntity entity) {
        return new CourseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCredits(),
                simpleCourseDtoMapper.map(entity.getPrerequisites()),
                mapToAssessment(entity.getCourseAssessments())
        );
    }

    private Set<CourseAssessmentDTO> mapToAssessment(Collection<CourseAssessmentValueObject> entities) {
        return entities.stream().map(e -> new CourseAssessmentDTO(
                e.assessmentType(), e.weight()
        )).collect(Collectors.toSet());
    }
}
