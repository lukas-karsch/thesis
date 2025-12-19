package karsch.lukas.features.lectures.queries;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "professor_projection")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ProfessorProjectionEntity {
    @Id
    private UUID id;

    private String firstName;

    private String lastName;
}
