package karsch.lukas.features.course.commands;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ICourseValidator {
    boolean allCoursesExist(Collection<UUID> ids);

    boolean courseExists(UUID id);

    int getCreditsForCourse(UUID id);

    int getMinimumCreditsToEnroll(UUID id);

    List<UUID> getPrerequisitesForCourse(UUID id);
}
