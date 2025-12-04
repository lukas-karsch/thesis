package karsch.lukas.features.course.api

import lombok.Getter
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

@Getter
data class CreateCourseCommand(
    @TargetAggregateIdentifier val courseId: UUID,
    val name: String,
    val description: String?,
    val credits: Int,
    val prerequisiteCourseIds: Set<UUID>,
    val minimumCreditsRequired: Int = 0
)
