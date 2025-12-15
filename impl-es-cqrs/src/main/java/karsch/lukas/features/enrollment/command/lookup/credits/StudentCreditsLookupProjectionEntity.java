package karsch.lukas.features.enrollment.command.lookup.credits;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_credits_lookup")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class StudentCreditsLookupProjectionEntity {

    @Id
    private UUID studentId;

    private int credits = 0;

    @Type(ListArrayType.class)
    @Column(
            name = "passed_courses",
            columnDefinition = "uuid[]"
    )
    private List<UUID> passedCourses = new ArrayList<>();
}
