package karsch.lukas.features.enrollment.exception;

import karsch.lukas.core.exceptions.MissingResourceException;

import java.util.UUID;

public class StudentNotEnrolledException extends MissingResourceException {
    public StudentNotEnrolledException(UUID lectureId, UUID studentId) {
        super(String.format("Enrollment not found for lectureId=%s, studentId=%s", lectureId, studentId));
    }
}
