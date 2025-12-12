package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.EnrollmentStatus;

import java.util.UUID;

public record EnrollmentStatusUpdate(UUID lectureId, UUID studentId, EnrollmentStatus status) {
}
