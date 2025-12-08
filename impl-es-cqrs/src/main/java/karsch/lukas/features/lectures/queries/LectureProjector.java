package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import karsch.lukas.course.CourseDTO;
import karsch.lukas.features.course.api.FindCourseByIdQuery;
import karsch.lukas.features.lectures.api.FindLectureByIdQuery;
import karsch.lukas.features.lectures.api.LectureCreatedEvent;
import karsch.lukas.features.professor.api.FindProfessorByIdQuery;
import karsch.lukas.lecture.LectureDetailDTO;
import karsch.lukas.lecture.TimeSlot;
import karsch.lukas.professor.ProfessorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@ProcessingGroup("lectures")
@RequiredArgsConstructor
public class LectureProjector {

    private final LectureRepository lectureRepository;
    private final QueryGateway queryGateway;
    private final ObjectMapper objectMapper;

    @EventHandler
    public void on(LectureCreatedEvent event) throws ExecutionException, InterruptedException, JsonProcessingException {
        // TODO add retries

        CompletableFuture<CourseDTO> courseDTO = queryGateway.query(new FindCourseByIdQuery(event.courseId()), ResponseTypes.instanceOf(CourseDTO.class));
        CompletableFuture<ProfessorDTO> professorDto = queryGateway.query(new FindProfessorByIdQuery(event.professorId()), ResponseTypes.instanceOf(ProfessorDTO.class));

        var lectureEntity = new LectureProjectionEntity();
        lectureEntity.setId(event.id());
        lectureEntity.setCourseId(event.courseId());
        lectureEntity.setLectureStatus(event.lectureStatus());

        lectureEntity.setCourseDtoJson(
                objectMapper.writeValueAsString(courseDTO.get())
        );

        lectureEntity.setDatesJson(
                objectMapper.writeValueAsString(event.dates())
        );

        lectureEntity.setProfessorId(event.professorId());
        lectureEntity.setProfessorDtoJson(
                objectMapper.writeValueAsString(professorDto.get())
        );

        lectureRepository.save(lectureEntity);
    }

    @QueryHandler
    public LectureDetailDTO findById(FindLectureByIdQuery query) throws JsonProcessingException {
        var found = lectureRepository.findById(query.lectureId()).orElse(null);

        if (found == null)
            return null;

        return new LectureDetailDTO(
                found.getId(),
                objectMapper.readValue(found.getCourseDtoJson(), CourseDTO.class),
                found.getMaximumStudents(),
                objectMapper.readerForListOf(TimeSlot.class).readValue(found.getDatesJson()),
                null,
                Collections.emptySet(),
                Collections.emptyList(),
                found.getLectureStatus(),
                Collections.emptySet()
        );
    }

}
