package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.users.ProfessorEntity;
import karsch.lukas.users.StudentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "lectures")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class LectureEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @Column(nullable = false)
    private int maximumStudents;

    @ElementCollection
    @CollectionTable(name = "lecture_timeslots", joinColumns = @JoinColumn(name = "lecture_id"))
    @OrderColumn(name = "slot_index")
    private List<TimeSlotValueObject> timeSlots = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private ProfessorEntity professor;

    @Column(nullable = false)
    private LectureStatus lectureStatus = LectureStatus.ARCHIVED;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdDate ASC")
    private List<LectureWaitlistEntryEntity> waitlist = new ArrayList<>();

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EnrollmentEntity> enrollments = new HashSet<>();

    @Transient
    public List<StudentEntity> getWaitlistedStudents() {
        return waitlist.stream()
                .map(LectureWaitlistEntryEntity::getStudent)
                .toList();
    }

    @Transient
    public Set<StudentEntity> getEnrolledStudents() {
        return enrollments.stream()
                .map(EnrollmentEntity::getStudent)
                .collect(Collectors.toSet());
    }
}
