package karsch.lukas.core.queries;

import java.util.Set;
import java.util.UUID;

public interface ICourseProjectionEntity {

    UUID getId();

    String getName();

    String getDescription();

    int getCredits();

    Set<UUID> getPrerequisiteCourseIds();

    int getMinimumCreditsRequired();

}
