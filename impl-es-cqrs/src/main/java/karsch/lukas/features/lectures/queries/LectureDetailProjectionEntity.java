package karsch.lukas.features.lectures.queries;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import karsch.lukas.lecture.LectureStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "lecture_projection")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LectureDetailProjectionEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private UUID courseId;

    private String courseDtoJson;

    private int maximumStudents;

    @Column(columnDefinition = "TEXT")
    private String datesJson;

    private UUID professorId;

    /**
     * @see karsch.lukas.professor.ProfessorDTO
     */
    @Column(columnDefinition = "TEXT")
    private String professorDtoJson;

    /**
     * @see karsch.lukas.student.StudentDTO
     */
    @Column(columnDefinition = "TEXT")
    private String enrolledStudentsDtoJson;

    /**
     * @see karsch.lukas.lecture.WaitlistEntryDTO
     */
    @Column(columnDefinition = "TEXT")
    private String waitingListDtoJson;

    private LectureStatus lectureStatus;

    /**
     * @see karsch.lukas.lecture.LectureAssessmentDTO
     */
    @Column(columnDefinition = "TEXT")
    private String assessmentsJson;
}
