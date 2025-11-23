package karsch.lukas.lectures;

import jakarta.persistence.*;
import karsch.lukas.audit.AuditableEntity;
import karsch.lukas.courses.CourseEntity;
import karsch.lukas.lecture.LectureStatus;
import karsch.lukas.time.TimeSlotComparator;
import karsch.lukas.time.TimeSlotValueObject;
import karsch.lukas.users.ProfessorEntity;
import karsch.lukas.users.StudentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SortComparator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "lectures")
@Getter
@Setter
@ToString(exclude = {"waitlist", "enrollments", "assessments"})
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

    @Column(nullable = false)
    private int minimumCreditsRequired = 0;

    @ElementCollection
    @CollectionTable(name = "lecture_timeslots", joinColumns = @JoinColumn(name = "lecture_id"))
    @SortComparator(TimeSlotComparator.class)
    private SortedSet<TimeSlotValueObject> timeSlots = new TreeSet<>(new TimeSlotComparator());

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private ProfessorEntity professor;

    @Column(nullable = false)
    private LectureStatus lectureStatus = LectureStatus.DRAFT;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdDate ASC")
    private List<LectureWaitlistEntryEntity> waitlist = new ArrayList<>();

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EnrollmentEntity> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LectureAssessmentEntity> assessments = new HashSet<>();

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

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
