package karsch.lukas.features.lectures.command.lookup.lecture;

import karsch.lukas.features.lectures.api.AssessmentAddedEvent;
import karsch.lukas.features.lectures.api.LectureCreatedEvent;
import karsch.lukas.features.lectures.api.LectureLifecycleAdvancedEvent;
import karsch.lukas.features.lectures.command.LectureAggregate;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@ProcessingGroup(LectureAggregate.PROCESSING_GROUP)
class LectureLookupProjector {

    private final LectureLookupRepository lectureLookupRepository;

    @EventHandler
    public void on(LectureCreatedEvent event) {
        var entity = new LectureLookupEntity(event.lectureId(), event.courseId(), event.professorId(), new ArrayList<>(), event.lectureStatus());
        lectureLookupRepository.save(entity);
    }

    @EventHandler
    public void on(AssessmentAddedEvent event) {
        var lecture = lectureLookupRepository.findById(event.lectureId()).orElseThrow(() -> new IllegalStateException("Lecture not found in lookup table."));
        lecture.getAssessmentIds().add(event.assessmentId());
    }

    @EventHandler
    public void on(LectureLifecycleAdvancedEvent event) {
        var lecture = lectureLookupRepository.findById(event.lectureId()).orElseThrow(() -> new IllegalStateException("Lecture not found in lookup table."));
        lecture.setLectureStatus(event.lectureStatus());
    }
}
