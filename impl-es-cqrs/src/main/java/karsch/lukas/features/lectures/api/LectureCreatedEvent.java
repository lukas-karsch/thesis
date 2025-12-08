package karsch.lukas.features.lectures.api;

import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.lecture.TimeSlot;

import java.util.List;
import java.util.UUID;

public record LectureCreatedEvent(
        UUID lectureId,
        UUID courseId,
        int maximumStudents,
        List<TimeSlot> dates,
        UUID professorId,
        LectureStatus lectureStatus) {
}
