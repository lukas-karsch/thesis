package karsch.lukas.course;

import java.util.UUID;

public record SimpleCourseDTO(UUID id, String name, String description, int credits) {
}
