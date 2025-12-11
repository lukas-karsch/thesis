package karsch.lukas.features.lectures.queries;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import karsch.lukas.lecture.GetLecturesForStudentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static karsch.lukas.core.json.Defaults.EMPTY_LIST;

/**
 * @see GetLecturesForStudentResponse
 */
@Entity
@Table(name = "student_lectures_projection")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentLecturesProjectionEntity {

    /**
     * The student's ID
     */
    @Id
    private UUID id;

    /**
     * List<LectureDto>
     */
    @Column(columnDefinition = "TEXT")
    private String enrolledJson = EMPTY_LIST;

    /**
     * List<LectureDto>
     */
    @Column(columnDefinition = "TEXT")
    private String waitlistedJson = EMPTY_LIST;

    @Type(ListArrayType.class)
    @Column(
            name = "enrolled_ids",
            columnDefinition = "uuid[]"
    )
    private List<UUID> enrolledIds = new ArrayList<>();

    @Type(ListArrayType.class)
    @Column(
            name = "waitlisted_ids",
            columnDefinition = "uuid[]"
    )
    private List<UUID> waitlistedIds = new ArrayList<>();

}
