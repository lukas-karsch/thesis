package karsch.lukas.users;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.lectures.EnrollmentEntity;
import karsch.lukas.uuid.GeneratedUuidV7;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = @Index(columnList = "semester", name = "idx_student_semester"))
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Audited
public class StudentEntity extends AuditableEntity {
    @Id
    @GeneratedUuidV7
    @EqualsAndHashCode.Include
    private UUID id;

    private String firstName;

    private String lastName;

    private int semester = 1;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<EnrollmentEntity> enrollments = new HashSet<>();

}
