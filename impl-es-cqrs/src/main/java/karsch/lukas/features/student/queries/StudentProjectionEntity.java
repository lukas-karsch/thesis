package karsch.lukas.features.student.queries;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentProjectionEntity {

    @Id
    private UUID id;

    private String firstName;

    private String lastName;

    private int semester;

}
