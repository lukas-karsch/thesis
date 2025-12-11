package karsch.lukas.features.lectures.queries;

import karsch.lukas.lecture.EnrollmentStatus;

import java.util.UUID;

public record EnrollmentStatusUpdate(UUID lectureId, UUID studentId, EnrollmentStatus status) {
}
