package karsch.lukas.course;

import java.util.Set;

public record CourseDTO(Long id, String name, String description, int credits, Set<SimpleCourseDTO> prerequisites) {
}
