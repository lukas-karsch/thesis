package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.users.StudentEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_waitlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureWaitlistEntryEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private LectureEntity lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @CreatedDate
    private LocalDateTime createdDate;

    @Override
    public String toString() {
        return "LectureWaitlistEntryEntity{" +
                "id=" + id +
                ", lecture=" + lecture.getId() +
                ", student=" + student.getId() +
                ", createdDate=" + createdDate +
                '}';
    }
}
