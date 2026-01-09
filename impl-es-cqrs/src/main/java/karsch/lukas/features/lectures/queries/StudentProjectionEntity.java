package karsch.lukas.features.lectures.queries;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "student_projection")
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
