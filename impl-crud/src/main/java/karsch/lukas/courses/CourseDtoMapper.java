package karsch.lukas.courses;

import karsch.lukas.course.CourseDTO;
import karsch.lukas.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
                simpleCourseDtoMapper.map(entity.getPrerequisites())
        );
    }
}
