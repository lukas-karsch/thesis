package karsch.lukas.features.lectures.command.lookup.timeSlot;

import karsch.lukas.core.lookup.TimeSlotEmbeddable;
import karsch.lukas.features.enrollment.command.lookup.EnrollmentLookupEntity;
import karsch.lukas.features.enrollment.command.lookup.IEnrollmentValidator;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.time.TimeSlotComparator;
import karsch.lukas.time.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TimeSlotValidator implements ITimeSlotValidator {

    private final LectureTimeslotLookupRepository lectureTimeslotLookupRepository;
    private final IEnrollmentValidator enrollmentValidator;
    private final TimeSlotService timeSlotService;

    @Override
    public boolean overlapsWithOtherLectures(SortedSet<TimeSlot> timeSlots, UUID studentId) {
        List<UUID> enrollments = enrollmentValidator.getEnrollmentsForStudent(studentId)
                .stream()
                .map(EnrollmentLookupEntity::getLectureId)
                .toList();

        List<LectureTimeslotLookupEntity> allStudentTimeslots = lectureTimeslotLookupRepository.findAllById(enrollments);

        return allStudentTimeslots.stream()
                .anyMatch(t -> timeSlotService.areConflictingTimeSlots(toTimeSlots(t.getTimeSlots()), timeSlots));
    }

    private SortedSet<TimeSlot> toTimeSlots(SortedSet<TimeSlotEmbeddable> timeSlots) {
        return timeSlots.stream()
                .map(t -> new TimeSlot(t.date(), t.startTime(), t.endTime()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(new TimeSlotComparator())));
    }
}
