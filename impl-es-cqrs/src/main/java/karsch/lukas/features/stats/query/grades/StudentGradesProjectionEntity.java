package karsch.lukas.features.stats.query.grades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "student_grades_projection")
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
class StudentGradesProjectionEntityId {
    private UUID studentId;
    private UUID lectureId;
}
