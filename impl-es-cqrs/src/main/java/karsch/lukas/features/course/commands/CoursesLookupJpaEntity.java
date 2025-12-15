package karsch.lukas.features.course.commands;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses_lookup")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
class CoursesLookupJpaEntity {
    @Id
    @NotNull
    private UUID id;

    private int credits;

    private int minimumCreditsRequired;

    @Type(ListArrayType.class)
    @Column(
            name = "prerequisite_courses",
            columnDefinition = "uuid[]"
    )
    private List<UUID> prerequisiteCourses = new ArrayList<>();

}
