package karsch.lukas.users;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.lectures.EnrollmentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "students", indexes = @Index(columnList = "semester", name = "idx_student_semester"))
@Getter
@Setter
@ToString
public class StudentEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private int semester;

    @OneToMany(mappedBy = "student")
    @ToString.Exclude
    private Set<EnrollmentEntity> enrollments;

}
