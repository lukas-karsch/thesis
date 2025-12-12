package karsch.lukas.features.enrollment.exception;

import karsch.lukas.core.exceptions.DomainException;

import java.util.UUID;

public class MissingGradeException extends DomainException {
    public MissingGradeException(UUID assessmentId, UUID studentId) {
        super("Can not update a grade that doesn't exist yet. (trying to update grade for assessmentId="
                + assessmentId
                + " and studentId="
                + studentId);
    }
}
