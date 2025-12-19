package karsch.lukas.features.stats.queries.grades;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stats__student_grades_projection")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(StudentGradesProjectionEntityId.class)
public class StudentGradesProjectionEntity {

    @Id
    private UUID studentId;

    @Id
    private UUID lectureId;

    @Column(columnDefinition = "TEXT")
    private String gradeDtoJson;

}

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
class StudentGradesProjectionEntityId {
    private UUID studentId;
    private UUID lectureId;
}
