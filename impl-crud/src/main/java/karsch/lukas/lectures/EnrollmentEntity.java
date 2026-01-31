package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.users.StudentEntity;
import karsch.lukas.uuid.GeneratedUuidV7;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@Audited
public class EnrollmentEntity extends AuditableEntity {

    @Id
    @GeneratedUuidV7
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private LectureEntity lecture;

    @CreatedDate
    @Column(updatable = false)
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
