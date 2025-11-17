package karsch.lukas.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for entities. Subclasses are monitored for changes and persisted to an audit log before any change. References
 * to other entities are references via the "id" field, they are not serialized.
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
