package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.LectureStatus;

import java.util.UUID;

public record LectureLifecycleAdvancedEvent(
        UUID lectureId,
        LectureStatus lectureStatus,
        UUID professorId) {
}
