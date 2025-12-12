package karsch.lukas.features.enrollment.exception;


import karsch.lukas.core.exceptions.MissingResourceException;

import java.util.UUID;

public class AssessmentNotFoundException extends MissingResourceException {
    public AssessmentNotFoundException(UUID uuid) {
        super(String.format("Assessment with id=%s doesn't exist", uuid));
    }
}
