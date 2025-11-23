package karsch.lukas.users;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.lectures.EnrollmentEntity;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "students", indexes = @Index(columnList = "semester", name = "idx_student_semester"))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class StudentEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String firstName;

    private String lastName;

    private int semester = 1;

    @OneToMany(mappedBy = "student")
    @ToString.Exclude
    private Set<EnrollmentEntity> enrollments;

}
