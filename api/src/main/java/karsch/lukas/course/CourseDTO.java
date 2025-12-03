package karsch.lukas.course;

import java.util.Set;
import java.util.UUID;

public record CourseDTO(UUID id, String name, String description, int credits, Set<SimpleCourseDTO> prerequisites,
                        int minimumCreditsRequired) {
}
