package karsch.lukas.features.enrollment.command.lookup;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "enrollment_lookup",
        indexes = {
                @Index(name = "lectureId_studentId_idx", columnList = "lecture_id,student_id"),
                @Index(name = "lectureId_idx", columnList = "lecture_id"),
                @Index(name = "studentId_idx", columnList = "student_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EnrollmentLookupEntity {

    @Id
    private UUID id;

    @Column(name = "lecture_id")
    private UUID lectureId;

    @Column(name = "student_id")
    private UUID studentId;

}
