package karsch.lukas.features.lectures.queries;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import karsch.lukas.core.queries.ICourseProjectionEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity(name = "lectures.CourseProjectionEntity")
@Getter
@Setter
@Table(name = "lectures__course_projection")
class CourseProjectionEntity implements ICourseProjectionEntity {
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

