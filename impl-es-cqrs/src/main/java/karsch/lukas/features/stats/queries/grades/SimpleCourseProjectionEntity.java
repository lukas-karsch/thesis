package karsch.lukas.features.stats.queries.grades;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stats__simple_course_projection")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleCourseProjectionEntity {

    @Id
    private UUID id;

    private String name;

    private int credits;

}
