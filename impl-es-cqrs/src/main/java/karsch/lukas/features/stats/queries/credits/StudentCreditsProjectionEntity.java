package karsch.lukas.features.stats.queries.credits;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "student_credits_projection")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentCreditsProjectionEntity {
    @Id
    private UUID id;

    private int totalCredits;
}
