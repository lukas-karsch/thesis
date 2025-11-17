package karsch.lukas.lectures;

import karsch.lukas.lecture.SimpleLectureDTO;
import karsch.lukas.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class SimpleLectureDtoMapper implements Mapper<LectureEntity, SimpleLectureDTO> {

    @Override
    public SimpleLectureDTO map(LectureEntity lectureEntity) {
        return new SimpleLectureDTO(
                lectureEntity.getId(),
                lectureEntity.getCourse().getId(),
                lectureEntity.getCourse().getName()
        );
    }
}
