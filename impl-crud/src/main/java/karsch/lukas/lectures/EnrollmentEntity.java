package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.users.StudentEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    @CreatedDate
    private LocalDateTime enrollmentDate;

    @Override
    public String toString() {
        return "EnrollmentEntity{" +
                "id=" + id +
                ", student=" + student.getId() +
                ", lecture=" + lecture.getId() +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
