package karsch.lukas.courses;

import karsch.lukas.course.SimpleCourseDTO;
import karsch.lukas.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class SimpleCourseDtoMapper implements Mapper<CourseEntity, SimpleCourseDTO> {

    @Override
    public SimpleCourseDTO map(CourseEntity courseEntity) {
        return new SimpleCourseDTO(
                courseEntity.getId(),
                courseEntity.getName(),
                courseEntity.getDescription(),
                courseEntity.getCredits()
        );
    }
}
