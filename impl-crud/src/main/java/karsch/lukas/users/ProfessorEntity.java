package karsch.lukas.users;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.lectures.LectureEntity;
import karsch.lukas.uuid.GeneratedUuidV7;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "professors")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ProfessorEntity extends AuditableEntity {
    @Id
    @GeneratedUuidV7
    @EqualsAndHashCode.Include
    private UUID id;

    private String firstName;

    private String lastName;

    @OneToMany(mappedBy = "professor")
    @ToString.Exclude
    private Set<LectureEntity> lectures;

}
