package karsch.lukas.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "audit_log",
        indexes = @Index(name = "audit_log_entityName_entityId_idx", columnList = "entity_name,entity_id")
)
@Getter
@Setter
@ToString
public class AuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "entity_name")
    private String entityName;

    @Column(name="entity_id")
    private UUID entityId;

    private String modifiedBy;

    @NotBlank
    private String operation;

    @NotNull
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String oldValueJson;

    @Column(columnDefinition = "TEXT")
    private String newValueJson;

    @Column(columnDefinition = "TEXT")
    private String contextJson;
}
