package karsch.lukas.features.stats.queries.gradeHistory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "stats__enrollment_projection",
        indexes = {@Index(name = "enrollment_projection__student_id_lecture_id_idx", columnList = "student_id,lecture_id")}
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class EnrollmentProjectionEntity {
    @Id
    UUID enrollmentId;

    @Column(name = "student_id")
    UUID studentId;

    @Column(name = "lecture_id")
    UUID lectureId;

}
