package karsch.lukas.features.lectures.command.lookup.lecture;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import karsch.lukas.lecture.LectureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lecture_lookup")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LectureLookupEntity {

    @Id
    private UUID id;

    private UUID courseId;

    private UUID professorId;

    @Type(ListArrayType.class)
    @Column(
            name = "assessment_ids",
            columnDefinition = "uuid[]"
    )
    private List<UUID> assessmentIds = new ArrayList<>();

    private LectureStatus lectureStatus;

}
