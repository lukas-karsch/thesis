package karsch.lukas.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for entities. Subclasses are monitored for changes and persisted to an audit log before any change.
 */
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class AuditableEntity {
    @Transient
    @JsonIgnore
    @Getter
    @Setter
    private String snapshotJson;
}
