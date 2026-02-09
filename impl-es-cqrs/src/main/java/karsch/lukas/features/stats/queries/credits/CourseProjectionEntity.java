package karsch.lukas.features.stats.queries.credits;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Entity(name = "credits.CourseProjectionEntity")
@Table(name = "stats__credits__course_projection")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class CourseProjectionEntity {
    @Id
    private UUID id;

    private int credits;
}
