package karsch.lukas.features.course.exceptions;

import karsch.lukas.core.exceptions.MissingResourceException;

import java.util.Collection;
import java.util.UUID;

public class MissingCoursesException extends MissingResourceException {
    public MissingCoursesException(Collection<UUID> missingIds) {
        super(String.format("Missing courses: IDs %s", missingIds));
    }

    public MissingCoursesException(String message) {
        super(message);
    }
}
