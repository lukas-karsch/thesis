package karsch.lukas.course;

import java.util.List;

public record CourseDTO(Long id, String name, String description, int credits, List<CourseDTO> prerequisites,
                        List<CourseAssessment> assessments) {
}
