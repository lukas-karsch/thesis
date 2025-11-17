package karsch.lukas.users;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.lectures.LectureEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "professors")
@Getter
@Setter
@ToString
public class ProfessorEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @OneToMany(mappedBy = "professor")
    @ToString.Exclude
    private Set<LectureEntity> lectures;

}
