package karsch.lukas.features.course.commands;

import java.util.Collection;
import java.util.UUID;

interface ICourseValidator {
    boolean allCoursesExist(Collection<UUID> ids);
}
