package karsch.lukas.features.lectures.exceptions;

import karsch.lukas.core.exceptions.MissingResourceException;

import java.util.UUID;

public class LectureNotFoundException extends MissingResourceException {
    public LectureNotFoundException(UUID lectureId) {
        super("Lecture with ID " + lectureId + " not found");
    }
}
