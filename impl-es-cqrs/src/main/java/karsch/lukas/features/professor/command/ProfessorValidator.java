package karsch.lukas.features.professor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
class ProfessorValidator implements IProfessorValidator {

    private final ProfessorLookupRepository professorLookupRepository;

    @Override
    public boolean existsById(UUID id) {
        return professorLookupRepository.existsById(id);
    }
}
