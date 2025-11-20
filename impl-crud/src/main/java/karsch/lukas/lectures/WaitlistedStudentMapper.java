package karsch.lukas.lectures;

import karsch.lukas.lecture.WaitlistedStudentDTO;
import karsch.lukas.mappers.Mapper;
import karsch.lukas.users.StudentDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitlistedStudentMapper implements Mapper<LectureWaitlistEntryEntity, WaitlistedStudentDTO> {
    private final StudentDtoMapper studentMapper;

    @Override
    public WaitlistedStudentDTO map(LectureWaitlistEntryEntity waitlistEntry) {
        return new WaitlistedStudentDTO(
                studentMapper.map(waitlistEntry.getStudent()),
                waitlistEntry.getCreatedDate()
        );
    }
}
