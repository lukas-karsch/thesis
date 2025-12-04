package karsch.lukas.features.course.api

import lombok.Getter
import java.util.*

@Getter
data class CourseCreatedEvent(
    val courseId: UUID,
    val name: String,
    val description: String?,
    val credits: Int,
    val prerequisiteCourseIds: Set<UUID> = HashSet(),
    val minimumCreditsRequired: Int
)
