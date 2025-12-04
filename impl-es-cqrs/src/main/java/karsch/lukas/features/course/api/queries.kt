package karsch.lukas.features.course.api

import lombok.Getter
import java.util.*

@Getter
data class FindCoursesByIdsQuery(val courseIds: Set<UUID>)

class FindAllCoursesQuery

