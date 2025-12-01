package karsch.lukas.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "courses")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private UUID courseId;
    private String name;
    private String description;
    private int credits;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> prerequisiteCourseIds;
    private int minimumCreditsRequired;
}
