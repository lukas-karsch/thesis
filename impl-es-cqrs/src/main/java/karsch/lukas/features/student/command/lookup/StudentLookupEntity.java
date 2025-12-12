package karsch.lukas.features.student.command.lookup;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "student_lookup")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentLookupEntity {

    @Id
    private UUID id;

    private int semester;

}
