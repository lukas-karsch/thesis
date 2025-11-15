package karsch.lukas.courses;

import karsch.lukas.course.CourseAssessmentDTO;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.mappers.Mapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseDtoMapper implements Mapper<CourseEntity, CourseDTO> {

    @Override
    public CourseDTO map(CourseEntity entity) {
        return new CourseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCredits(),
                mapToSimple(entity.getPrerequisites()),
                mapToAssessment(entity.getCourseAssessments())
        );
    }

    private Set<SimpleCourseDTO> mapToSimple(Collection<CourseEntity> entities) {
        return entities.stream().map(e -> new SimpleCourseDTO(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getCredits()
        )).collect(Collectors.toSet());
    }

    private Set<CourseAssessmentDTO> mapToAssessment(Collection<CourseAssessmentValueObject> entities) {
        return entities.stream().map(e -> new CourseAssessmentDTO(
                e.assessmentType(), e.weight()
        )).collect(Collectors.toSet());
    }
}
