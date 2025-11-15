package karsch.lukas.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@ToString
public class AuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String entityName;

    private Long entityId;

    private String modifiedBy;

    @NotBlank
    private String operation;

    @NotNull
    private LocalDateTime timestamp;

    private String oldValueJson;

    private String newValueJson;
}
