package karsch.lukas.features.student.command.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentValidator implements IStudentValidator {
    private final StudentLookupRepository studentLookupRepository;

    @Override
    public boolean existsById(UUID id) {
        return studentLookupRepository.existsById(id);
    }

    @Override
    public Optional<StudentLookupEntity> findById(UUID id) {
        return studentLookupRepository.findById(id);
    }

    @Override
    public List<StudentLookupEntity> findByIds(List<UUID> ids) {
        return studentLookupRepository.findAllById(ids);
    }
}
