package karsch.lukas.features.course.queries;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "courses")
class CourseProjectionEntity {
    @Id
    @Column(unique = true)
    private UUID id;
    private String name;
    private String description;
    private int credits;
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<UUID> prerequisiteCourseIds;
    private int minimumCreditsRequired;
}
