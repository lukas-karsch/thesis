package karsch.lukas.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import karsch.lukas.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AuditEntityListener {

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    @Setter
    private static AuditLogRepository auditLogRepository; // TODO try to remove this code smell (static setter)

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(AuditEntityListener.class);

    private final RequestContext requestContext;

    @PostLoad
    public void postLoad(Object entity) {
        try {
            if (entity instanceof AuditableEntity auditable) {
                auditable.setSnapshotJson(objectMapper.writeValueAsString(entity));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        saveAuditLog(entity, CREATE, null);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            saveAuditLog(entity, UPDATE, auditable.getSnapshotJson());
        } else {
            saveAuditLog(entity, UPDATE, null);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        saveAuditLog(entity, DELETE, null);
    }

    private void saveAuditLog(Object entity, String operation, String oldJson) {
        try {
            var log = new AuditLogEntry();
            log.setEntityName(entity.getClass().getSimpleName());
            log.setOperation(operation);
            log.setTimestamp(LocalDateTime.now());
            log.setModifiedBy(
                    String.format("%s_%d", requestContext.getUserType(), requestContext.getUserId())
            );

            log.setOldValueJson(oldJson);
            if (!DELETE.equals(operation)) {
                log.setNewValueJson(objectMapper.writeValueAsString(entity));
            }

            try {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                log.setEntityId((Long) idField.get(entity));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
